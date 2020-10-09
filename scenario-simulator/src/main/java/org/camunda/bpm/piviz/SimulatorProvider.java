package org.camunda.bpm.piviz;

import java.io.InputStream;
import java.util.List;

public interface SimulatorProvider {

	class HistoryBase {
		public String activityInstanceId;
		public String taskId;
		public long timeSinceProcessStart;
		public String executionId;
		public boolean consumed = false;
	};
	
	class ActivityHistory extends HistoryBase {
		public long endTimeSinceProcessStart;
		public String activityId;
		public ActivityState state;
	};
	
	class VariableHistory extends HistoryBase {
		public String name;
		public Object value;
		public boolean atProcessStart;
	}
	
	enum ActivityState {
		RUNNING,
		COMPLETED,
		CANCELLED,
		ERROR
	};
	
	String getProcessInstanceId();
	
	String getProcessDefinitionId();
	
	String getProcessDefinitionKey();
	
	InputStream getProcessDefinition();
	
	List<HistoryBase> getHistory();
	
}
