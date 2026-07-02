/**
 * 
 */
package com.trinet.ambis.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSErrorResponseCodes;
import com.trinet.ambis.persistence.dao.ps.PsDao;
import com.trinet.ambis.service.impl.StrategyServiceImpl;
import com.trinet.common.AppConfig;
import com.openhtmltopdf.pdfboxout.PdfContentStreamAdapter.PdfException;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

/**
 * @author rvutukuri
 *
 */
public class CommonUtils {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtils.class);

	private CommonUtils() {
		throw new IllegalStateException(
				"Utility class " + CommonUtils.class.getName() + " can not be instantiated.");
	}
	
	/**
	 * This method is for logging the caught exceptions in a similar format.
	 * 
	 * @param ex
	 * @param logger
	 */
	public static void logExceptions(Exception ex, Logger logger, String companyCode, String emplId) {
		try {
			BSSApplicationError appError = null;
			if (ex instanceof BSSApplicationException) {
				appError = ((BSSApplicationException) ex).getBssError();
				if (null != ((BSSApplicationException) ex).getCause()) {
					appError.setMessage(((BSSApplicationException) ex).getCause().toString());
					appError.setSource(((BSSApplicationException) ex).getStackTrace()[0].getClassName());
					appError.setExceptionStackTrace(
							ExceptionUtils.getStackTrace(((BSSApplicationException) ex).getCause()));
				}
			} else {
				appError = new BSSApplicationError(BSSErrorResponseCodes.BSS_UNHANDLED_EXCEPTION,
						BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, "", "UNHANDLED BSS EXCEPTIONS", "", null);
				appError.setMessage(ex.toString());
				appError.setSource(ex.getStackTrace()[0].getClassName());
				appError.setExceptionStackTrace(ExceptionUtils.getStackTrace(ex));

			}
			appError.setCompanyCode(companyCode);
			appError.setEmplId(emplId);
			ObjectMapper mapper = new ObjectMapper();
			String errorToLog = mapper.writeValueAsString(appError);
			logger.error("BSS_ERROR: {}", errorToLog);
		} catch (Exception e) {
			logger.error("Error processing the exception object");
		}
	}
	
	/**
	 * Returns the collection of buckets of given size for given list.
	 * @param list
	 * @param size
	 * @return
	 */
	public static Collection<List<String>> getBucketedList(List<String> list, int size) {
		if (null == list || list.isEmpty()) {
			return Collections.emptyList();
		}
		final AtomicInteger counter = new AtomicInteger(0);
		return list.stream().collect(Collectors.groupingBy(it -> counter.getAndIncrement() / size)).values();
	}

	/**
	 * Returns true if the application is pointing to production DB.
	 * 
	 * @param psDao
	 * @return
	 */
	public static boolean isProduction(PsDao psDao) {
		boolean isProduction = false;
		if ("HRPROD".equals(psDao.getDatabase())) {
			isProduction = true;
		}
		return isProduction;
	}

	/**
	 * Convert a Date of String type from one format to another.
	 *
	 * @param date
	 * @param fromFormat
	 * @param toFormat
	 * @return a formatted date String
	 */
	public static String formatDate(String date, String fromFormat, String toFormat) {
		if( date == null ) {
			return "";
		}
		try {
			return new SimpleDateFormat(toFormat).format(new SimpleDateFormat(fromFormat).parse(date));
		} catch (ParseException e) {
			throw new BSSApplicationException(e,
					new BSSApplicationError(BSSErrorResponseCodes.BSS_UNHANDLED_EXCEPTION,
							BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, StrategyServiceImpl.class.getName(),
							"Format string to date conversion failed", null, null));
		}
	}

	/**
	 * Convert a Date to a String using a specified format.
	 *
	 * @param date
	 * @param format
	 * @return a formatted date String
	 */
	public static String formatDateToString( Date date, String format ) {
		if( date == null ) {
			return "";
		}
		String formattedDate = null;
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		formattedDate = formatter.format(date);
		return formattedDate;
	}

	/**
	 * This method is for formatting date into a specific format.
	 * 
	 * @param date
	 * @param format
	 * @return
	 */
	public static Date formatStringToDate(String date, String format) {
		if( date == null ) {
			return null;
		}
		Date formattedDate = null;
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		try {
			formattedDate = formatter.parse(date);
		} catch (ParseException e) {
			throw new BSSApplicationException(e,
					new BSSApplicationError(BSSErrorResponseCodes.BSS_UNHANDLED_EXCEPTION,
							BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, StrategyServiceImpl.class.getName(),
							"Format string to date conversion failed", null, null));
		}
		return formattedDate;
	}	
	
	/**
	 * This method returns true if given benOfferTypeName (OFFER TYPE CODE WILL NOT
	 * WORK) is present in the given map of benOfferExceptions
	 * 
	 * @param benOfferExceptions
	 * @param benOfferTypeName
	 * @return
	 */
	public static boolean isBenOfferExceptionAvailable(Map<String, Boolean> benOfferExceptions,
			String benOfferTypeName) {
		boolean result = false;
		if (BSSApplicationConstants.BSUPP.equals(benOfferTypeName)
				|| BSSApplicationConstants.WAIVER_ALLOWANCE.equals(benOfferTypeName)) {
			result = benOfferExceptions.get(PlanTypesEnum.MEDICAL.getCode()) == null ? false
					: benOfferExceptions.get(PlanTypesEnum.MEDICAL.getCode());
		} else if (BSSApplicationConstants.DISABILITY.equals(benOfferTypeName)) {
			if (null != benOfferExceptions.get(PlanTypesEnum.STD.getCode())) {
				result = benOfferExceptions.get(PlanTypesEnum.STD.getCode());
			} else if (null != benOfferExceptions.get(PlanTypesEnum.LTD.getCode())) {
				result = benOfferExceptions.get(PlanTypesEnum.LTD.getCode());
			}
		} else {
			// Dental, Vision, CMTR and LIFE
			result = benOfferExceptions.get(PlanTypesEnum.getCode(benOfferTypeName)) == null ? false
					: benOfferExceptions.get(PlanTypesEnum.getCode(benOfferTypeName));
		}
		return result;
	}
	
	public static <T> T createNewObjectUsing(Object sourceObj, Class<T> destObjectClass) {
		T destObj = null;
		try {
			destObj = destObjectClass.getDeclaredConstructor().newInstance();
			java.util.Date defaultValue = null;
			DateConverter converter = new DateConverter(defaultValue);
			ConvertUtils.register(converter, java.util.Date.class);
			ConvertUtils.deregister(Date.class);

			ConvertUtilsBean convertUtilsBean = BeanUtilsBean.getInstance().getConvertUtils();
		    convertUtilsBean.register(false, true, 0);
			BeanUtils.copyProperties(destObj, sourceObj);

			BeanUtils.copyProperties(destObj, sourceObj);
		} catch (Exception e) {
			throw new BSSApplicationException(e,
					new BSSApplicationError(BSSErrorResponseCodes.BSS_UNHANDLED_EXCEPTION,
							BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, StrategyServiceImpl.class.getName(),
							"Object conversion failed", null, null));
		}
		return destObjectClass.cast(destObj);
	}


	/**
	 * This method compares and returns the lesser of two java.util.Date objects.
	 *
	 * @param date1
	 * @param date2
	 * @return the lesser of the two arguments.  If one argument is null, this returns
	 * the other.  If both arguments are null, this returns null.
	 */
	public static Date chooseLesserDate( Date date1, Date date2 ) {
		if( date1 == null && date2 == null ) {
			return null;
		} else if( date1 == null ) {
			return date2;
		} else if( date2 == null ) {
			return date1;
		}

		if( date1.compareTo( date2 ) < 0 ) {
			return date1;
		} else {
			return date2;
		}
	}


	/**
	 * This method compares and returns the greater of two java.util.Date objects.
	 * This can be useful when choosing a date that must not be less than some other
	 * minimum date.
	 * 
	 * @param testDate
	 * @param minimumDate
	 * @return the greater of the two arguments.  If one argument is null, this returns
	 * the other.  If both arguments are null, this returns null.
	 */
	public static Date chooseGreaterDate( Date testDate, Date minimumDate ) {
		if( testDate == null && minimumDate == null ) {
			return null;
		} else if( testDate == null ) {
			return minimumDate;
		} else if( minimumDate == null ) {
			return testDate;
		}
		
		if( testDate.compareTo( minimumDate ) > 0 ) {
			return testDate;
		} else {
			return minimumDate;
		}
	}

	/**
	 * This method was created specifcally to transform a number representing a month
	 * to a two-character String.  Other formats can be passed in the second argument.
	 * <p>
	 * This method must not return null.  It should return a single space if the first
	 * argument is null.
	 * 
	 * @param month
	 * @param fmt an instance of NumberFormat or one of its implementations
	 * @return the formatted number or a single space if the first argument was null
	 */
	public static String formatMonth( Integer month, NumberFormat fmt ) {
		if( null == month ) {
			return " ";
		} else {
			return fmt.format( month );
		}
	}

	/**
	 * One of a set of methods to resolve null parameter values.  If the argument is null,
	 * this method will return a single space.
	 * @param parm an argument to be tested against null.
	 * @return the original argument or a single space if the argument is null.
	 */
	public static String validateParameter( String parm ) {
		return CommonUtils.validateParameter( parm, " " );
	}

	/**
	 * One of a set of methods to resolve null parameter values. The first argument is
	 * evaluated.  If it is not null, it is returned.  If the first argument is null,
	 * the second argument is returned.
	 * @param parm an argument to be tested against null.
	 * @param ifNullDefault the default value to be returned if the first argument is null.
	 * @return the original argument or the second argument if the first is null.
	 */
	public static String validateParameter( String parm, String ifNullDefault ) {
		if( null == parm || "".equals( parm ) ) {
			return ifNullDefault;
		} else {
			return parm;
		}
	}

	/**
	 * One of a set of methods to resolve null parameter values.  If the argument is null,
	 * this method will return zero.
	 * @param parm an argument to be tested against null.
	 * @return the original argument or zero if the argument is null.
	 */
	public static Integer validateParameter( Integer parm ) {
		return CommonUtils.validateParameter( parm, 0 );
	}

	/**
	 * One of a set of methods to resolve null parameter values. The first argument is
	 * evaluated.  If it is not null, it is returned.  If the first argument is null,
	 * the second argument is returned.
	 * @param parm an argument to be tested against null.
	 * @param ifNullDefault the default value to be returned if the first argument is null.
	 * @return the original argument or the second argument if the first is null.
	 */
	public static Integer validateParameter( Integer parm, Integer ifNullDefault ) {
		if( null == parm ) {
			return ifNullDefault;
		} else {
			return parm;
		}
	}

	/**
	 * One of a set of methods to resolve null parameter values.  If the argument is null,
	 * this method will return zero.
	 * @param parm an argument to be tested against null.
	 * @return the original argument or zero if the argument is null.
	 */
	public static BigDecimal validateParameter( BigDecimal parm ) {
		return CommonUtils.validateParameter( parm, BigDecimal.ZERO );
	}

	/**
	 * One of a set of methods to resolve null parameter values. The first argument is
	 * evaluated.  If it is not null, it is returned.  If the first argument is null,
	 * the second argument is returned.
	 * @param parm an argument to be tested against null.
	 * @param ifNullDefault the default value to be returned if the first argument is null.
	 * @return the original argument or the second argument if the first is null.
	 */
	public static BigDecimal validateParameter( BigDecimal parm, BigDecimal ifNullDefault ) {
		if( null == parm ) {
			return ifNullDefault;
		} else {
			return parm;
		}
	}
	
	public static boolean checkIfDateIsInRangeInclusive(java.util.Date dateToVerify, java.util.Date startDate,
			java.util.Date endDate) {
		return (dateToVerify.compareTo(startDate) >= 0 && dateToVerify.compareTo(endDate) <= 0);
	}

	public static String prepareFinalRequestUri(String uri, Map<String, ?> urlParams) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath(uri);
		return AppConfig.getMicroServiceURL() + builder.buildAndExpand(urlParams).toUriString();
	}
	
	public static File convertHtmlToPdf(String fileName, String fileExtension, String htmlStr)
			throws IOException, PdfException {
		Path tempPdfFile = null;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		PdfRendererBuilder builder = new PdfRendererBuilder();
		try {
			htmlStr = convertToXhtml(htmlStr);
			builder.withHtmlContent(htmlStr,null);
			builder.useFastMode();
			builder.toStream(os);
			builder.run();
			byte[] pdfAsBytes = os.toByteArray();
			tempPdfFile = Files.createTempFile(fileName, fileExtension);
			Files.write(tempPdfFile, pdfAsBytes);
			Files.write(tempPdfFile, os.toByteArray());
		} catch (IOException | PdfException e) {
			LOGGER.error("Error converting html to pdf. Exception :: %s", e);
		} finally {
			try {
				os.close();
			} catch (Exception e) {
				LOGGER.error("Error converting html to pdf");
			}
		}
		return tempPdfFile.toFile();
	}

	public static Date getCurrentDate() {
		Date currentDate;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		Calendar cal = Calendar.getInstance();
		String cDate = sdf.format(cal.getTime());

		try {
			currentDate = sdf.parse(cDate);
		} catch (ParseException e) {
			currentDate = new Date();
		}
		return currentDate;
	}
	
	private static String convertToXhtml(String html) {
		final Document document = Jsoup.parse(html);
	    document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
	    return document.html();
	}
}
