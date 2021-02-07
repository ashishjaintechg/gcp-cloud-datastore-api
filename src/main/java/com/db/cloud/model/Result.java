package com.db.cloud.model;

	

import java.util.List;

public class Result<T> {
	public String cursor;
	public List<T> result;

	public Result(List<T> result, String cursor) {
		this.result = result;
		this.cursor = cursor;
	}

	public Result(List<T> result) {
		this.result = result;
		this.cursor = null;
	}
}
