/**
 * 
 */
package com.trinet.ambis.rest.controllers;

import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.email.EmailService;
import com.trinet.ambis.service.email.dto.ClientConversionRequestDto;
import com.trinet.ambis.util.BSSSecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author kpamulapati
 */

@RestController
@RequestMapping(URIConstants.VERSION_AND_ROOT)
@Api(value = "Trinet API-BSS Email Controller")
public class EmailController {

	private static final Logger logger = LoggerFactory.getLogger(EmailController.class);

	@Autowired
	private EmailService emailService;
	
	@Autowired
	private CompanyService companyService;

	@GetMapping(value = URIConstants.SEND_EMAIL)
	@ApiOperation(value = "Sends email")
	@ResponseBody
	public void sendEmail(HttpServletRequest request, @PathVariable("confirmationId") final String confirmationNumber,
			@PathVariable("companyCode") final String companyCode) {
		logger.info("In sendEmail() code: {}", companyCode);
		Company company = companyService.getCompanyDetails(companyCode, false, BSSSecurityUtils.getAuthenticatedPersonId(), null);
		emailService.resendConfirmationEmail(company, confirmationNumber);
	}

}