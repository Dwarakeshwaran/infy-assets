package utilities.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsvToString {

	private static final Logger logger = LoggerFactory.getLogger(CsvToString.class);

	public List<String> csvToBatch(File file, int limit) {
		int j = 0;
		int div;
		int size;
		try (Scanner sc = new Scanner(file)) {

			sc.useDelimiter("\r");
			List<String> list = new ArrayList<>();

			List<String> listSender = new ArrayList<>();

			while (sc.hasNext()) {
				list.add(sc.next());
			}

			StringBuilder sb = new StringBuilder();
			StringBuilder sbHeader = new StringBuilder();
			StringBuilder sbSender = new StringBuilder();
			sbHeader.append(list.get(0));

			logger.info("number of lines {}", list.size() - 1);

			size = list.size();
			div = size / limit;

			for (int i = 0; i <= div; i++) {

				if (((i + 1) * limit) > size)
					j = size - 1;
				else
					j = (i + 1) * limit;

				for (int index = (i * limit) + 1; index <= j; index++)
					sb.append(list.get(index));

				sbSender.append(sbHeader).append(sb);

				logger.info("Message {}", sbSender);

				listSender.add(sbSender.toString());
				sb.delete(0, sb.length());
				sbSender.delete(0, sbSender.length());

			}
			return listSender;
		} catch (Exception e) {

			logger.error("Error Message {}", e.getMessage());
			return Collections.emptyList();
		}

	}

}
