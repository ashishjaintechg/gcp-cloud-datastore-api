package com.db.cloud.exception;

/**
 * This class CloudDatastoreDAOException returns exceptions related to Cloud
 * data store.
 *
 * @author Ashish Jain
 * @version 1.0
 * @date 13-DEC-2018
 *
 */
public class CloudDatastoreDAOException extends RuntimeException {
	private static final long	serialVersionUID	=1l;
	public final static int	ERROR_CODE = 1001;
	/**
	 * This refers to exception error code.
	 */
	private int errorCode;

	/**
	 * This CloudDatastoreDAOException constructor passes exception message to super
	 * class.
	 * 
	 * @param String msg - Exception message
	 */
	public CloudDatastoreDAOException(String msg) {
		super(msg);
	}

	/**
	 * This CloudDatastoreDAOException constructor passes error message to super
	 * class.
	 * 
	 * @param String msg - Exception message
	 * @param        int errorCode - Exception code
	 */

	public CloudDatastoreDAOException(String msg, int errorCode) {
		super(msg);
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}

}