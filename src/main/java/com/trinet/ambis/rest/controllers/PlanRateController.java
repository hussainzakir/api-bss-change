/**
 * 
 */
package com.trinet.ambis.rest.controllers;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.PlanRatesService;
import com.trinet.ambis.service.model.PlanRatesExportData;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.FileUtils;
import com.trinet.common.DateUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author kpamulapati
 */

@RestController
@RequestMapping(URIConstants.VERSION_AND_ROOT)
@Api(value = "Trinet API-BSS Plan Rate Controller")
public class PlanRateController {

	@Autowired
	CompanyService companyService;

	@Autowired
	PlanRatesService planRatesService;

	private static final Logger logger = LoggerFactory.getLogger(PlanRateController.class);

	// Generic API call to force download the excel with given filename.
	@PostMapping(value = URIConstants.PLAN_RATES_EXPORT_TO_EXCEL)
	@ApiOperation(value = "Downloads plan rates to excel file", notes="Contributions Page")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Plan rates downloaded to excel successfully") })
	public void planRatesDownloadToExcel(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String dateTime = DateUtils.convertDateToString(new Date(), DateUtils.DATE_FORMAT_TIME).replaceAll(" ", "-");
		StringBuilder fileName = new StringBuilder("PlanRates-").append(dateTime);
		response.setContentType("application/vnd.ms-excel");
		response.setHeader("content-disposition", "attachment; filename=" + fileName.toString() + ".xls");
		String sData = request.getParameter("data");
		byte[] bytes = sData.getBytes();
		ServletOutputStream os = response.getOutputStream();
		try {
			response.setContentLength(bytes.length);
			if (sData != null) {
				os.write(bytes);
			}
		} finally {
			os.flush();
			os.close();
		}
	}

	@GetMapping(value = URIConstants.GET_PLAN_RATES_FOR_EXPORT)
	@ApiOperation(value = "Gets plan rates from excel file", notes="Contributions page", response = PlanRatesExportData.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Plan rates exported successfully") })
	@ResponseBody
	public PlanRatesExportData getPlanRatesForExport(HttpServletRequest request,
			@PathVariable("companyCode") String code,
			@RequestParam(required = false, defaultValue = "") String exchangeId) {
		long startTime = System.currentTimeMillis();
		Company company = companyService.getCompanyDetails(code, false, BSSSecurityUtils.getAuthenticatedPersonId(),
				BenExchngEnums.getByExchangeId(exchangeId));
		PlanRatesExportData planRatesExportData = planRatesService.getPlanRatesExportData(company);
		long endTime = System.currentTimeMillis();
		logger.info("getPlanRatesForExport() took {} ms", (endTime - startTime));
		return planRatesExportData;
	}

	@PostMapping(value = URIConstants.GET_SELECTED_PLAN_RATES_EXPORT)
	@ApiOperation(value = "Gets the selected plans rates into excel file", notes="Contributions - Selected Plans")
	@ResponseBody
	public void generatePlanRatesExcelFile(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("company-code") final String companyCode,
			@RequestParam("hiddenColumns") final String hiddenDataColumns,
			@RequestParam("data") final String requestData,
			@RequestParam(required = false, defaultValue = "") String exchangeId) throws IOException {
		long startTime = System.currentTimeMillis();
		Company company = companyService.getCompanyDetails(companyCode, false,
				BSSSecurityUtils.getAuthenticatedPersonId(), BenExchngEnums.getByExchangeId(exchangeId));
		
		PlanRatesExportData planRatesExportData =  CommonServiceHelper.jsonToObject(requestData, PlanRatesExportData.class);
		Workbook workbook = planRatesService.getPlanRatesExcelWorkbook(company, planRatesExportData, hiddenDataColumns);

		String companyName = FileUtils.removeSpecialCharacters(company.getName());
		response.setHeader("Content-disposition", "attachment; filename=" + companyName + "_Plan_Rates.xlsx");
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		workbook.write(response.getOutputStream());

		long endTime = System.currentTimeMillis();
		logger.info("generatePlanRatesExcelFile() took {} ms", (endTime - startTime));
	}
}
