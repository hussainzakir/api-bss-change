package com.trinet.ambis.enums;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author jshuali
 */
public enum BenExchngEnums {

	// @formatter:off
 	TRINET_I(5, "TriNet I", "ACD", Set.of("AC"), "TNI", "Exchange I"), 
 	TRINET_II(4, "TriNet II", "SOI", Set.of("SM", "SY"), "TNII", "Exchange II"),
	TRINET_III(3, "TriNet III", "PAS", Set.of("Q1", "Q2", "Q3", "Q4", "Q5"), "TNIII", "Exchange III"),
	TRINET_IV(1, "TriNet IV", "AMB", Set.of("8Y"), "TNIV", "Exchange IV"), 
	TRINET_XI(2, "TriNet XI", "ALP", Set.of("AL"), "TNXI", "Exchange XI"),
	TRINET_OMS(6, "TriNet OMS", "OMS", Set.of("A1"), "OMS", "Exchange OMS");
	// @formatter:on

	private final long id;
	private final String benExchng;
	private final String product;
	private final Set<String> quarters;
	private final String exchangeId;
	private final String exchangeName;

	private static final Map<Long, BenExchngEnums> byId = new HashMap<>();
	static {
		for (BenExchngEnums e : BenExchngEnums.values()) {
			if (byId.put(e.getId(), e) != null) {
				throw new IllegalArgumentException("duplicate id: " + e.getId());
			}
		}
	}

	public static BenExchngEnums getById(Long id) {
		return byId.get(id);
	}

	public long getId() {
		return id;
	}

	public String getBenExchng() {
		return benExchng;
	}

	public String getProduct() {
		return product;
	}

	public Set<String> getQuarters() {
		return quarters;
	}

	public String getExchangeId() {
		return exchangeId;
	}

	public String getExchangeName() {
		return exchangeName;
	}

	private BenExchngEnums(long id, String benExchng, String product, Set<String> quarters, String exchangeId,
			String exchangeName) {
		this.id = id;
		this.benExchng = benExchng;
		this.product = product;
		this.quarters = quarters;
		this.exchangeId = exchangeId;
		this.exchangeName = exchangeName;
	}

	public static BenExchngEnums getByQuarter(String quarter) {
		Optional<BenExchngEnums> foundExchange = Arrays.asList(BenExchngEnums.values()).stream()
				.filter(benExchngEnum -> benExchngEnum.getQuarters().contains(quarter)).findFirst();
		if (foundExchange.isPresent()) {
			return foundExchange.get();
		}
		throw new IllegalArgumentException(quarter + " is not mapped to any of the exchanges.");
	}


	/**
	 * Find a BenExchngEnums matching the given exchangeId parameter
	 * @param exchangeId
	 * @return the matching enum or null if not matched
	 */
	public static BenExchngEnums getByExchangeId(String exchangeId) {
		Optional<BenExchngEnums> optExchange = Arrays.stream( BenExchngEnums.values() )
				.filter( x -> x.getExchangeId().equals( exchangeId ) )
				.findFirst();
		if( optExchange.isPresent() )
			return optExchange.get();
		else
			return null;
	}
	
	/**
	 * Find a BenExchngEnums matching the given benExchange parameter
	 * benExchange = TriNet I/TriNet II/TriNet III/TriNet IV/TriNet XI
	 * 
	 * @param benExchange
	 * @return
	 */
	public static BenExchngEnums getByBenExchange(String benExchange) {
		Optional<BenExchngEnums> optExchange = Arrays.stream( BenExchngEnums.values() )
				.filter( x -> x.getBenExchng().equals( benExchange ) )
				.findFirst();
		if( optExchange.isPresent() )
			return optExchange.get();
		else
			return null;
	}

	/**
	 * This methods returns all the quarters
	 * 
	 * @return set of all quarters
	 */
	public static Set<String> getAllQuarters() {
		return Arrays.asList(BenExchngEnums.values()).stream().map(exchange -> exchange.getQuarters())
				.collect(Collectors.toSet()).stream().flatMap(Set::stream).collect(Collectors.toSet());
	}

	/**
	 * This method checks the existence of given quarter
	 * 
	 * @param quarter
	 * @return true if quarter is matched with the existing 10 quarters <br>
	 *         false if quarter is not matched with the existing 10 quarters
	 */
	public static boolean isValidQuarter(String quarter) {
		return getAllQuarters().contains(quarter);
	}

}
