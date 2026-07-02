package com.trinet.ambis.persistence.dao.ps.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.persistence.dao.ps.HSAPlansDao;
import com.trinet.ambis.service.model.BenDefnOptnHSA;
import com.trinet.ambis.util.DaoUtils;

@Repository
public class HSAPlansDaoImpl implements HSAPlansDao {

	private EntityManager em;

	// SQL parameters, names and display constants
	private static final String BENEFIT_PROGRAM = "benefitProgram";
	private static final String EFFDT = "effdt";
	
	private static final String NEW_PLAN = "newPlan";
	private static final String CLONE_PLAN = "clonePlan";
	private static final String PF_CLIENT = "pfClient";
	private static final String EE_CONTRIB = "eeContrib";
	private static final String FAM_CONTRIB = "famContrib";
	
	private static final String ALL_HSA_PLANS = "GET_ALL_HSA_PLANS_FOR_BENPROG";
	private static final String ACTIVE_MED_PLANS = "GET_ACTIVE_MED_PLANS_FOR_BENPROG";
	private static final String UPD_BENEF_PLAN = "UPDATE_BENEF_PLAN_TBL";
	private static final String UPD_FSA_BENEF = "UPDATE_FSA_BENEF_TBL";
	private static final String DEL_CONTRIB = "DELETE_HSA_CONTRIB_LMT";
	private static final String INS_CONTRIB = "INSERT_HSA_CONTRIB_LMT";
	private static final String INS_LIM_INCLUDE = "INSERT_HSA_LIMIT_INCLD";
	private static final String UPDATE_OPTN = "UPDATE_HSA_OPTN";

	private static final String ERR_BSS_HSA = "ERR_BSS_HSA";

	public HSAPlansDaoImpl( EntityManager em ) {
		super();
		this.setEntityManager( em );
	}

	/**
	 * This constructor only exists to allow for unit testing.
	 */
	public HSAPlansDaoImpl() {
		super();
	}
	
	@Override
	public void setEntityManager( EntityManager em ) {
		this.em = em;
	}


	@Override
	public List<BenDefnOptnHSA> getAllHSAPlans( String benefitProgram, java.sql.Date effdt ) {

		List<BenDefnOptnHSA> optns = new ArrayList<>();
		String sqlName = ALL_HSA_PLANS;
		
		// select all HSA plans for the benefit program
		Query query = em.createNamedQuery( sqlName );
		query.setParameter( BENEFIT_PROGRAM, benefitProgram );
		query.setParameter( EFFDT, effdt );
		Map<String, Object> queryMap = DaoUtils.generateQueryMap(query);
		
		// execute the query and parse the result as a List
		try {
			List<Object[]> results = DaoUtils.getResultList(query, ALL_HSA_PLANS);
			
			for( Object[] row : results ) {
				BenDefnOptnHSA opt = new BenDefnOptnHSA();
				opt.setBenefitProgram((String) row[0]);
				opt.setEffdt((String) row[1]);
				opt.setPlanType((String) row[2]);
				opt.setOptionId((BigDecimal) row[3]);
				opt.setDisplayOptSeq((BigDecimal) row[4]);
				opt.setOptionType((String) row[5]);
				opt.setBenefitPlan((String) row[6]);
				opt.setCovrgCd((String) row[7]);
				opt.setOptionCd((String) row[8]);
				opt.setOptionLvl((BigDecimal) row[9]);
				opt.setDedcd((String) row[10]);
				opt.setDfltOptionInd((String) row[11]);
				opt.setEligRulesId((String) row[12]);
				opt.setLocationTblId((String) row[13]);
				opt.setCrossPlanType((String) row[14]);
				opt.setCrossBenefPlan((String) row[15]);
				opt.setCoverageLimitPct((BigDecimal) row[16]);
				opt.setCrossPlnDpndChk((String) row[17]);
				opt.setPfClient((String) row[18]);
				optns.add( opt );
			}
		} catch( Exception e ) {
			this.generateException( e, queryMap, sqlName );
		}

		return optns;

	}


	@Override
	public Map<String,String> getActiveMedPlans( String benefitProgram, java.sql.Date effdt ) {

		Map<String,String> medPlansEligMap = new HashMap<>();
		String sqlName = ACTIVE_MED_PLANS;
		
		// select all HSA plans for the benefit program
		Query query = em.createNamedQuery( sqlName );
		query.setParameter( BENEFIT_PROGRAM, benefitProgram );
		query.setParameter( EFFDT, effdt );
		
		// execute the query and parse the result as a List
		List<?> results = DaoUtils.getResultList(query, sqlName);
		for( Object o : results ) {
			Object[] row = (Object[]) o;
			String benefitPlan = (String) row[0];
			String eligRulesId = (String) row[1];
			medPlansEligMap.put( benefitPlan, eligRulesId );
		}

		return medPlansEligMap;

	}


	@Override
	public int updateBenefPlanTable( String cloneBenefitPlan, String newBenefitPlan,
			String pfClient, java.sql.Date effdt ) {

		String sqlName = UPD_BENEF_PLAN;
		int rowCount = 0;
		
		// update BENEF_PLAN_TBL rows for this setup
		Query query = em.createNamedQuery( sqlName );
		query.setParameter( NEW_PLAN, newBenefitPlan );
		query.setParameter( CLONE_PLAN, cloneBenefitPlan );
		query.setParameter( EFFDT, effdt );
		query.setParameter( PF_CLIENT, pfClient );
		Map<String, Object> queryMap = DaoUtils.generateQueryMap(query);
		
		try {
			rowCount = DaoUtils.executeUpdate( query, sqlName );
		} catch( Exception e ) {
			this.generateException( e, queryMap, sqlName );
		}
		return rowCount;

	}


	@Override
	public int updateFSABenefTable( String cloneBenefitPlan, String newBenefitPlan, java.sql.Date effdt ) {

		String sqlName = UPD_FSA_BENEF;
		int rowCount = 0;
		
		// update BENEF_PLAN_TBL rows for this setup
		Query query = em.createNamedQuery( sqlName );
		query.setParameter( NEW_PLAN, newBenefitPlan );
		query.setParameter( CLONE_PLAN, cloneBenefitPlan );
		query.setParameter( EFFDT, effdt );
		Map<String, Object> queryMap = DaoUtils.generateQueryMap(query);

		try {
			rowCount = DaoUtils.executeUpdate( query, sqlName );
		} catch( Exception e ) {
			this.generateException( e, queryMap, sqlName );
		}
		return rowCount;

	}


	@Override
	public int deleteHSAContribLmt( String newBenefitPlan, Date effdt ) {

		String sqlName = DEL_CONTRIB;
		int rowCount = 0;

		// delete rows from HSA_CONTRIB_LMT before inserting for cleanest submit
		Query query = em.createNamedQuery( sqlName );
		query.setParameter( NEW_PLAN, newBenefitPlan );
		query.setParameter( EFFDT, effdt );
		Map<String, Object> queryMap = DaoUtils.generateQueryMap(query);

		try {
			rowCount = DaoUtils.executeUpdate( query, sqlName );
		} catch( Exception e ) {
			this.generateException( e, queryMap, sqlName );
		}

		return rowCount;
	}


	@Override
	public int insertHSAContribLmt( String cloneBenefitPlan, String newBenefitPlan, BigDecimal eeContrib,
			BigDecimal famContrib, Date effdt ) {

		String sqlName = INS_CONTRIB;
		int rowCount = 0;
		
		// insert the rows defining the company contribution amounts
		Query query = em.createNamedQuery( sqlName );
		query.setParameter( NEW_PLAN, newBenefitPlan );
		query.setParameter( EFFDT, effdt );
		query.setParameter( EE_CONTRIB, eeContrib );
		query.setParameter( FAM_CONTRIB, famContrib );
		query.setParameter( CLONE_PLAN, cloneBenefitPlan );
		Map<String, Object> queryMap = DaoUtils.generateQueryMap(query);

		try {
			rowCount = DaoUtils.executeUpdate( query, sqlName );
		} catch( Exception e ) {
			this.generateException( e, queryMap, sqlName );
		}
		return rowCount;

	}


	@Override
	public int updateLimitIncludeTable( String newBenefitPlan, Date effdt ) {

		String sqlName = INS_LIM_INCLUDE;
		int rowCount = 0;
		
		// insert the limit (deductions) include table
		Query query = em.createNamedQuery( sqlName );
		query.setParameter( NEW_PLAN, newBenefitPlan );
		query.setParameter( EFFDT, effdt );
		Map<String, Object> queryMap = DaoUtils.generateQueryMap(query);

		try {
			rowCount = DaoUtils.executeUpdate( query, sqlName );
		} catch( Exception e ) {
			this.generateException( e, queryMap, sqlName );
		}
		return rowCount;

	}


	@Override
	public int updateHSAOptns( List<BenDefnOptnHSA> optns ) {

		String sqlString = em.createNamedQuery( UPDATE_OPTN )
				.unwrap(org.hibernate.query.Query.class).getQueryString();

		int rowCount = optns.size();

		Session session = em.unwrap( Session.class );
		session.doWork( ( Connection cn ) -> {
			PreparedStatement stmt = cn.prepareStatement( sqlString );
			
			for( BenDefnOptnHSA op : optns ) {
				stmt.setString( 1, op.getBenefitProgram() );
				stmt.setDate( 2, java.sql.Date.valueOf( op.getEffdt() ) );
				stmt.setString( 3, op.getPlanType() );
				stmt.setBigDecimal( 4, op.getOptionId() );
				stmt.setString( 5, op.getBenefitProgram() );
				stmt.setDate( 6, java.sql.Date.valueOf( op.getEffdt() ) );
				stmt.setBigDecimal( 7, op.getOptionId() );
				stmt.setBigDecimal( 8, op.getDisplayOptSeq() );
				stmt.setString( 9, op.getOptionType() );
				stmt.setString( 10, op.getBenefitPlan() );
				stmt.setString( 11, op.getCovrgCd() );
				stmt.setString( 12, op.getOptionCd() );
				stmt.setBigDecimal( 13, op.getOptionLvl() );
				stmt.setString( 14, op.getDedcd() );
				stmt.setString( 15, op.getDfltOptionInd() );
				stmt.setString( 16, op.getEligRulesId() );
				stmt.setString( 17, op.getLocationTblId() );
				stmt.setString( 18, op.getCrossPlanType() );
				stmt.setString( 19, op.getCrossBenefPlan() );
				stmt.setBigDecimal( 20, op.getCoverageLimitPct() );
				stmt.setString( 21, op.getCrossPlnDpndChk() );
				stmt.addBatch();
			}
			try {
				stmt.executeBatch();
			} catch( SQLException sqlEx ) {
				this.generateException( sqlEx, null, UPDATE_OPTN );
			}
		});

		return rowCount;
	}



	/**
	 * This private method will generate the standard exception for this class
	 * @param e  the Exception object caught by a method of this class
	 * @param sql
	 * @param sqlStatementName
	 */
	private void generateException( Exception e, Map<String,Object> sqlParameters, String sqlStatementName ) {
		
		BSSApplicationError errorData = new BSSApplicationError( ERR_BSS_HSA,
				BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, this.getClass().getName()
				, "Failure executing SQL for HSA plan setup.", sqlStatementName,
				sqlParameters );
		throw new BSSApplicationException( e, errorData );

	}
}
