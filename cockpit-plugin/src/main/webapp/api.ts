export interface ProcessInstanceDiagramOverlayParams {
  processInstanceId: string;
  api: any;
}

export interface Element {
  id: string;
  status: 'RUNNING' | 'COMPLETED' | 'SUSPENDED' | 'ERROR' | 'CANCELLED';
}

export interface Report {
  elements: Array<Element>;
}
