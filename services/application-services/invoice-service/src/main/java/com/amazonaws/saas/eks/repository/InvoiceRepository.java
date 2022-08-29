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
package com.amazonaws.saas.eks.repository;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.saas.eks.model.Invoice;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.TableNameOverride;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;

@Repository
public class InvoiceRepository {
	private static final Logger logger = LogManager.getLogger(InvoiceRepository.class);

	/**
	 * Method to get all Invoices for a tenant
	 * @param tenantId
	 * @return List<Invoice>
	 */
	public List<Invoice> getInvoices(String tenantId) {
		PaginatedScanList<Invoice> results = null;
		DynamoDBMapper mapper = dynamoDBMapper(tenantId);
		
		try {
			results = mapper.scan(Invoice.class, new DynamoDBScanExpression());
		} catch (Exception e) {
			logger.error("TenantId: " + tenantId + "-Get Invoices failed " + e.getMessage());
		}

		return results;
	}

	/**
	 * Method to save an Invoice for a tenant
	 * @param invoice
	 * @param tenantId
	 * @return Invoice
	 */
	public Invoice save(Invoice invoice, String tenantId) {
		try {
			DynamoDBMapper mapper = dynamoDBMapper(tenantId);
			mapper.save(invoice);
		} catch (Exception e) {
			logger.error("TenantId: " + tenantId + "-Save Invoice failed " + e.getMessage());
		}
		
		return invoice;
	}

	/**
	 * Method to get Invoice by Id for a tenant
	 * @param invoiceId
	 * @param tenantId
	 * @return Invoice
	 */
	public Invoice getInvoiceById(String invoiceId, String tenantId) {
		DynamoDBMapper mapper = dynamoDBMapper(tenantId);
		Invoice invoice = null;
		
		DynamoDBMapperConfig config = DynamoDBMapperConfig.builder()
				.withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT).build();
		try {
			invoice = mapper.load(Invoice.class, invoiceId, config);
		} catch (Exception e) {
			logger.error("TenantId: " + tenantId + "-Get Invoice By Id failed " + e.getMessage());
		}
		
		return invoice;
	}

	/**
	 * Method to delete a tenant's invoice
	 * @param invoice
	 * @param tenantId
	 */
	public void delete(Invoice invoice, String tenantId) {
		try {
			DynamoDBMapper mapper = dynamoDBMapper(tenantId);
			mapper.delete(invoice);
		} catch (Exception e) {
			logger.error("TenantId: " + tenantId + "-Delete Invoice failed " + e.getMessage());
		}
	}
	
	/**
	 * Method to retrieve DynamoDBMapper and access to the tenant's Invoice table
	 * @param tenantId
	 * @return DynamoDBMapper
	 */
	public DynamoDBMapper dynamoDBMapper(String tenantId) {
		String tableName = "Invoice-" + tenantId;
		DynamoDBMapperConfig dbMapperConfig = new DynamoDBMapperConfig.Builder()
				.withTableNameOverride(TableNameOverride.withTableNameReplacement(tableName)).build();

		AmazonDynamoDBClient dynamoClient = getAmazonDynamoDBLocalClient(tenantId);
		return new DynamoDBMapper(dynamoClient, dbMapperConfig);
	}

	/**
	 * Helper method for DynamoDBMapper
	 * @param tenantId
	 * @return AmazonDynamoDBClient
	 */
	private AmazonDynamoDBClient getAmazonDynamoDBLocalClient(String tenantId) {
		return (AmazonDynamoDBClient) AmazonDynamoDBClientBuilder.standard()
				// .withCredentials(WebIdentityTokenCredentialsProvider.builder().roleSessionName("ddb-query").build())
				.withCredentials(new DefaultAWSCredentialsProviderChain()).build();
	}

}
