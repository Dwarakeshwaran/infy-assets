package s3.handler;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import s3.service.Service;

public class Handler implements RequestHandler<Map<String, String>, Object> {

	public static void main(String[] args) {

		Service service = new Service();
		service.doSftpTransfer();

	}

	public Object handleRequest(Map<String, String> input, Context context) {

		return null;
	}

}
