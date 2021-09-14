package com.tw.fixture;

import com.thoughtworks.fusheng.integration.junit5.FuShengTest;
import com.tw.CdiContainer;

@FuShengTest
public class CdiApplicationTest {
	private CdiContainer cdiContainer;

	public void init(String rootClass) throws ClassNotFoundException {
		Class<?> clazz = Class.forName(rootClass);
		cdiContainer = CdiContainer.initContainer(clazz);
	}

	public Integer getComponentSize(String className) throws ClassNotFoundException {
		Class<?> clazz = Class.forName(className);
		return cdiContainer.getBean(clazz).size();
	}

	public String getComponent(String className) throws ClassNotFoundException {
		Class<?> clazz = Class.forName(className);
		try {
			return cdiContainer.getBean(clazz).get(0).getClass().getName();
		} catch (RuntimeException e) {
			return e.getMessage();
		}
	}

}
