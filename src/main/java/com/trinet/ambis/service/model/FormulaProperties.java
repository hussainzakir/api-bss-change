package com.trinet.ambis.service.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class FormulaProperties {
	private String planType;
	private String benefitPlan;
	private String formulaID;
	private Date formulaEffDt;
	private String baseSource;
	private BigDecimal maxBenefitBase;
	private BigDecimal minCovrg;
	private BigDecimal maxCovrg;
	private java.sql.Date coverageAsOfDate;
	private java.sql.Date premiumAsOfDate;
	private List<FormulaDefinition> formulaDefs;

	/**
	 * Using the benefit formula properties, calculate the coverage base for Life and AD&D plans
	 * @param salary
	 * @return the coverage base for a life insurance or AD&D plan
	 */
	public BigDecimal calculateLifeADDCoverage( BigDecimal salary ) {

		// do some life AD&D specific setup before calling the solver, if needed

		BigDecimal covrg = this.solveFormula( salary );

		// test against coverage min and max
		if( this.getMaxCovrg() != null && ! this.getMaxCovrg().equals( BigDecimal.ZERO ) ) {
			if( covrg.compareTo( this.getMaxCovrg() ) > 0 ) {
				covrg = this.getMaxCovrg();
			}
		}

		if( this.getMinCovrg() != null && covrg.compareTo( this.getMinCovrg() ) < 0 ) {
			covrg = this.getMinCovrg();
		}

		// apply age coverage reduction

		return covrg;
	}


	/**
	 * Using the benefit formula properties, calculate the coverage base for disability plans
	 * @param salary
	 * @return the coverage base for a disability plan
	 */
	public BigDecimal calculateDisabilityCoverage( BigDecimal salary ) {

		// do some disability-specific setup before calling the solver

		BigDecimal covrg = this.solveFormula( salary );
		return covrg;

		// do we need salary replacement calculation?  does this affect the cost?
	}

	/**
	 * PeopleSoft coverage formula calculation, ported from the Cobol PSPDCOVG.
	 * Cobol code here refers to paragraph MA300-SOLVE-FORMULA in the PeopleSoft program.
	 * @param base
	 * @return calculated coverage amount
	 */
	private BigDecimal solveFormula( BigDecimal base ) {
		final String CNST_ENTRY = "CNST";
		final String BASE_ENTRY = "BASE";

		BigDecimal calBase = BigDecimal.ZERO;

		// MOVE ZERO TO W-CNSTVALUE
		BigDecimal wCnstValue = BigDecimal.ZERO;

		// PERFORM VARYING FRLDEF-IDX FROM 1 BY 1
		//   UNTIL FRLDEF-IDX > FRML-DEF-COUNT OF FRLTB(FRLTB-IDX)
		for( FormulaDefinition def : this.formulaDefs ) {
			switch( def.getBenOperand() ) {
/*			EVALUATE TRUE
			WHEN BEN-OPERAND OF FRLTB(FRLTB-IDX FRLDEF-IDX) = SPACE
			WHEN BEN-OPERAND OF FRLTB(FRLTB-IDX FRLDEF-IDX) = '+'
			WHEN BEN-OPERAND OF FRLTB(FRLTB-IDX FRLDEF-IDX) = '-'
			WHEN BEN-OPERAND OF FRLTB(FRLTB-IDX FRLDEF-IDX) = '/'
			WHEN BEN-OPERAND OF FRLTB(FRLTB-IDX FRLDEF-IDX) = '*'
*/			case " ":
			case "+":
			case "-":
			case "/":
			case "*":
				//  IF BN-ENTRY-TYP OF FRLTB(FRLTB-IDX FRLDEF-IDX)
				//                                             = 'CNST'
				if( CNST_ENTRY.equals( def.getBnEntryTyp() ) ) {
					// MOVE BN-VALUE OF FRLTB(FRLTB-IDX FRLDEF-IDX)
					//      TO W-CNSTVALUE OF W-WK
					wCnstValue = def.getBnValue();
				// END-IF
				}

				// IF BN-ENTRY-TYP OF FRLTB(FRLTB-IDX FRLDEF-IDX)
				//                                             = 'BASE'
				if( BASE_ENTRY.equals( def.getBnEntryTyp() ) ) {
					//    PERFORM TA000-CALC-COVERAGE
					BigDecimal calcMaxBase = base;
					if( this.getMaxBenefitBase() != null 
							&& this.getMaxBenefitBase().compareTo( BigDecimal.ZERO ) > 0 
							&& this.getMaxBenefitBase().compareTo( calcMaxBase ) < 0 ) {
						calcMaxBase = this.getMaxBenefitBase();
					}

					//    MOVE CALCULATED-BASE OF DARRY(DARRY-IDX)
					//         TO W-CALBASE OF W-WK
					if( calcMaxBase == null ) {
						calBase = BigDecimal.ZERO;
					} else {
						calBase = calcMaxBase;
					}

				//  END-IF
				}
				break;
			// WHEN BEN-OPERAND OF FRLTB(FRLTB-IDX FRLDEF-IDX) = '('
			// WHEN BEN-OPERAND OF FRLTB(FRLTB-IDX FRLDEF-IDX) = ')'
			case ")":
			case "(":
				// CONTINUE
				break;
			// WHEN BEN-OPERAND OF FRLTB(FRLTB-IDX FRLDEF-IDX) = 'R'
			case "R":
				// PERFORM SA000-ROUND-COVERAGE
				calBase = FormulaProperties.roundCoverage( calBase, def.getRoundUpAmt(), def.getRoundTo() );
				// MOVE ZERO TO W-CNSTVALUE
				wCnstValue = BigDecimal.ZERO;
				break;
			default:
				//System.out.println( "other operator:" + def.getBenOperand() + ":" );
			// END-EVALUATE
			}

			// IF BEN-OPERAND OF FRLTB(FRLTB-IDX FRLDEF-IDX) = SPACE
			if( " ".equals( def.getBenOperand() ) ) {
				// IF BN-ENTRY-TYP OF FRLTB(FRLTB-IDX FRLDEF-IDX) = 'CNST'
				if( CNST_ENTRY.equals( def.getBnEntryTyp() ) ) {
					// MOVE BN-VALUE OF FRLTB(FRLTB-IDX FRLDEF-IDX)
					//         TO W-CALBASE OF W-WK
					calBase = def.getBnValue();
				// END-IF
				}
			// END-IF
			}

			// IF BEN-OPERAND OF FRLTB(FRLTB-IDX FRLDEF-IDX) = '+'
			if( "+".equals( def.getBenOperand()) ) {
				// IF BN-ENTRY-TYP OF FRLTB(FRLTB-IDX FRLDEF-IDX) = 'CNST'
				if( CNST_ENTRY.equals( def.getBnEntryTyp() ) ) {
					// COMPUTE W-CALBASE OF W-WK =
					//                      W-CALBASE OF W-WK +
					//                      W-CNSTVALUE OF W-WK
					calBase = calBase.add( wCnstValue );
				// ELSE
				} else {
					// IF BN-ENTRY-TYP OF FRLTB(FRLTB-IDX FRLDEF-IDX) = 'BASE'
					if( BASE_ENTRY.equals( def.getBnEntryTyp() ) ) {
						// IF W-CNSTVALUE OF W-WK NOT = ZERO
						if( ! BigDecimal.ZERO.equals( wCnstValue ) ) {
							// COMPUTE W-CALBASE OF W-WK =
							//                      W-CALBASE OF W-WK +
							//                      W-CNSTVALUE OF W-WK
							calBase = calBase.add( wCnstValue );
						// END-IF
						}
					// END-IF
					}
				// END-IF
				}
			// END-IF
			}

			// IF BEN-OPERAND OF FRLTB(FRLTB-IDX FRLDEF-IDX) = '-'
			if( "-".equals( def.getBenOperand() ) ) {
				// IF BN-ENTRY-TYP OF FRLTB(FRLTB-IDX FRLDEF-IDX) = 'CNST'
				if( CNST_ENTRY.equals( def.getBnEntryTyp() ) ) {
					// COMPUTE W-CALBASE OF W-WK =
					//                      W-CALBASE OF W-WK -
					//                      W-CNSTVALUE OF W-WK
					calBase = calBase.subtract( wCnstValue );
				// ELSE
				} else {
					// IF BN-ENTRY-TYP OF FRLTB(FRLTB-IDX FRLDEF-IDX) = 'BASE'
					if( BASE_ENTRY.equals( def.getBnEntryTyp() ) ) {
						// IF W-CNSTVALUE OF W-WK NOT = ZERO
						if( ! BigDecimal.ZERO.equals( wCnstValue ) ) {
							// COMPUTE W-CALBASE OF W-WK =
							//                      W-CNSTVALUE OF W-WK -
							//                      W-CALBASE OF W-WK
							calBase = calBase.subtract( wCnstValue );
						// END-IF
						}
					// END-IF
					}
				// END-IF
				}
			// END-IF
			}

			// IF BEN-OPERAND OF FRLTB(FRLTB-IDX FRLDEF-IDX) = '*'
			if( "*".equals( def.getBenOperand() ) ) {
				// IF BN-ENTRY-TYP OF FRLTB(FRLTB-IDX FRLDEF-IDX) = 'CNST'
				if( CNST_ENTRY.equals( def.getBnEntryTyp() ) ) {
					// COMPUTE W-CALBASE OF W-WK =
					//                      W-CALBASE OF W-WK *
					//                      W-CNSTVALUE OF W-WK
					calBase = calBase.multiply( wCnstValue );
				// ELSE
				} else {
					// IF BN-ENTRY-TYP OF FRLTB(FRLTB-IDX FRLDEF-IDX) = 'BASE'
					if( BASE_ENTRY.equals( def.getBnEntryTyp() ) ) {
						// IF W-CNSTVALUE OF W-WK NOT = ZERO
						if( ! BigDecimal.ZERO.equals( wCnstValue ) ) {
							//COMPUTE W-CALBASE OF W-WK =
							//                     W-CALBASE OF W-WK *
							//                     W-CNSTVALUE OF W-WK
							calBase = calBase.multiply( wCnstValue );
						// END-IF
						}
					// END-IF
					}
				// END-IF
				}
			// END-IF
			}

			// IF BEN-OPERAND OF FRLTB(FRLTB-IDX FRLDEF-IDX) = '/'
			if( "/".equals( def.getBenOperand() ) ) {
				// IF BN-ENTRY-TYP OF FRLTB(FRLTB-IDX FRLDEF-IDX) = 'CNST'
				if( CNST_ENTRY.equals( def.getBnEntryTyp() ) ) {
					// COMPUTE W-CALBASE OF W-WK =
					//                      W-CALBASE OF W-WK /
					//                      W-CNSTVALUE OF W-WK
					calBase = calBase.divide( wCnstValue, RoundingMode.HALF_UP );
				// ELSE
				} else {
					// IF BN-ENTRY-TYP OF FRLTB(FRLTB-IDX FRLDEF-IDX) = 'BASE'
					if( BASE_ENTRY.equals( def.getBnEntryTyp() ) ) {
						// IF W-CNSTVALUE OF W-WK NOT = ZERO
						if( ! BigDecimal.ZERO.equals( wCnstValue ) ) {
							// COMPUTE W-CALBASE OF W-WK =
							//                      W-CNSTVALUE OF W-WK /
							//                      W-CALBASE OF W-WK
							calBase = wCnstValue.divide( calBase, RoundingMode.HALF_UP );
						// END-IF
						}
					// END-IF
					}
				// END-IF
				}
			//END-IF
			}

		// END-PERFORM
		}

		// MOVE W-CALBASE OF W-WK
		//      TO  CALCULATED-BASE OF DARRY(DARRY-IDX)
		return calBase;
	}

	/**
	 * PeopleSoft coverage formula rounding calculation, ported from the Cobol PSPDCOVG.
	 * Cobol code here refers to paragraph SA000-ROUND-COVERAGE in the PeopleSoft program.
	 * If any parameter is null, the value assigned to 'base' is returned.
	 * @param base
	 * @param roundUpAmt
	 * @param roundTo
	 * @return the rounded value or base if any argument is null
	 */
	private static BigDecimal roundCoverage( BigDecimal base, BigDecimal roundUpAmt, BigDecimal roundTo ) {

		// short-circuit method of not all parameters have been supplied
		if( base == null || roundUpAmt == null || roundTo == null ) {
			return base;
		}

		// COMPUTE WK-ROUND-UNITS OF W-WK
		//           =  W-CALBASE OF W-WK
		//           /  ROUND-TO OF FRLTB(FRLTB-IDX FRLDEF-IDX)
		BigDecimal wkRoundUnits = base.divide( roundTo, RoundingMode.HALF_UP ).setScale( 0, RoundingMode.DOWN );

		// IF ROUND-UP-AMT OF FRLTB(FRLTB-IDX FRLDEF-IDX)
		//         <=  (W-CALBASE OF W-WK
		//                  -  (WK-ROUND-UNITS OF W-WK
		//                  *  ROUND-TO
		//                           OF FRLTB(FRLTB-IDX FRLDEF-IDX)))
		if( roundUpAmt.compareTo( base.subtract( wkRoundUnits.multiply( roundTo ) )) <= 0 ) {
			//     ADD 1  TO  WK-ROUND-UNITS OF W-WK
			wkRoundUnits = wkRoundUnits.add( BigDecimal.ONE );
		// END-IF
		}

		// COMPUTE W-CALBASE OF W-WK
		//         =  WK-ROUND-UNITS OF W-WK
		//         *  ROUND-TO OF FRLTB(FRLTB-IDX FRLDEF-IDX)
		BigDecimal newBase = wkRoundUnits.multiply( roundTo );

		// ROUND-COVERAGE-EXIT.
		return newBase;
	}

}
