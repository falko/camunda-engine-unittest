package org.camunda.bpm.unittest;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

//@Named
public class ThrowBpmnErrorJavaDelegate implements JavaDelegate {

  public void execute(DelegateExecution arg0) throws Exception {
    throw new BpmnError("error-code-not-caught-by-process");
  }

}
