package com.trinet.ambis.rest.controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.service.BenConfirmationStatementService;
import com.trinet.ambis.service.model.BenConfirmationStatement;

/**
 * @author tallam
 */

@RestController
@RequestMapping(URIConstants.VERSION_AND_ROOT)
public class BenConfirmationStatementsController {
	
	private static final Logger logger = LoggerFactory.getLogger(BenConfirmationStatementsController.class);

	@Autowired
	private BenConfirmationStatementService benConfirmationStatementService;
	
	@GetMapping(value = URIConstants.BEN_CONFIRMATION_STATEMENT)
	@ResponseBody
	public List<BenConfirmationStatement> getConfirmationStatements(HttpServletRequest request,
			@PathVariable("companyCode") String companyCode) {
		logger.info("In BenConfirmationStatement Rest End point: getConfirmationStatements for companyCode : {} ", companyCode);
		return benConfirmationStatementService.getBenConfirmationStatementsBy(companyCode);
	}
}
