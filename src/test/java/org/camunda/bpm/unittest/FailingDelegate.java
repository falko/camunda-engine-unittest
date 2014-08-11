package org.camunda.bpm.unittest;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class FailingDelegate implements JavaDelegate {

	public void execute(DelegateExecution execution) throws Exception {
		throw new Exception();
	}

}
