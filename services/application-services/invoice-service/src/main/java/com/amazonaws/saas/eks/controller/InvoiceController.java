/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.amazonaws.saas.eks.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.saas.eks.auth.TokenManager;
import com.amazonaws.saas.eks.model.Invoice;
import com.amazonaws.saas.eks.service.InvoiceService;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class InvoiceController {
	private static final Logger logger = LogManager.getLogger(InvoiceController.class);

	@Autowired
	private InvoiceService invoiceService;

	@Autowired
	private TokenManager tokenManager;

	/**
	 * Method to retrieve all invoices for a tenant
	 * 
	 * @param request
	 * @return List<Invoice>
	 */
	@GetMapping(value = "{companyName}/invoice/api/invoices", produces = { MediaType.APPLICATION_JSON_VALUE })
	public List<Invoice> getInvoices(HttpServletRequest request) {
		logger.info("Return Invoices");
		String tenantId = null;
		List<Invoice> invoices = null;

		try {
			tenantId = tokenManager.getTenantId(request);
			
			if (tenantId != null && !tenantId.isEmpty()) {
				invoices =  invoiceService.getInvoices(tenantId);
				return invoices;
			}
		} catch (Exception e) {
			logger.error("TenantId: " + tenantId + "-get invoices failed: ", e);
			return null;
		}

		return invoices;
	}

	/**
	 * Method to get Invoice by id for a tenant
	 * 
	 * @param invoiceId
	 * @param request
	 * @return Invoice
	 */
	@GetMapping(value = "{companyName}/invoice/api/invoice/{invoiceId}", produces = { MediaType.APPLICATION_JSON_VALUE })
	public Invoice getInvoiceById(@PathVariable("invoiceId") String invoiceId, HttpServletRequest request) {
		String tenantId = null;
		Invoice invoice = null;
		
		try {
			tenantId = tokenManager.getTenantId(request);
			
			if (tenantId != null && !tenantId.isEmpty()) {
				invoice = invoiceService.getInvoiceById(invoiceId, tenantId);
				return invoice;
			}
		} catch (Exception e) {
			logger.error("TenantId: " + tenantId + "-get invoice by ID failed: ", e);
			return null;
		}

		return invoice;
	}

	/**
	 * Method to save an invoice for a tenant
	 * 
	 * @param invoice
	 * @param request
	 * @return Invoice
	 */
	@PostMapping(value = "{companyName}/invoice/api/invoice", produces = { MediaType.APPLICATION_JSON_VALUE })
	public Invoice saveInvoice(@RequestBody Invoice invoice, HttpServletRequest request) {
		String tenantId = null;
		Invoice newInvoice = null;
		
		try {
			tenantId = tokenManager.getTenantId(request);
			if (tenantId != null && !tenantId.isEmpty()) {
				newInvoice = invoiceService.save(invoice, tenantId);
				return newInvoice;
			}
		} catch (Exception e) {
			logger.error("TenantId: " + tenantId + "-save invoice failed: ", e);
			return null;
		}

		return newInvoice;
	}

	@RequestMapping("{companyName}/invoice/health/invoice")
	public String health() {
		return "\"Invoice service is up!\"";
	}

}