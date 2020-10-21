interface ProcessInstanceDiagramOverlayParams {
  processInstanceId: string;
  api: any;
}

const plugin = {
  id: "piviz-plugin",
  pluginPoint: "cockpit.processInstance.diagram.plugin",
  priority: 0,
  render: (viewer: any, params: ProcessInstanceDiagramOverlayParams) => {
    console.warn(params);
    console.warn(viewer);
  }
};

export default plugin;
