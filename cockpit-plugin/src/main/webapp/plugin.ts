import * as Api from './api';

const plugin = {
  id: "piviz-plugin",
  pluginPoint: "cockpit.processInstance.diagram.plugin",
  priority: 0,
  render: (viewer: any, params: Api.ProcessInstanceDiagramOverlayParams) => {

    const cockpitApi = params.api.cockpitApi;
    const engine = params.api.engine;

    fetch(`${cockpitApi}/plugin/piviz-plugin/${engine}/process-instance/${params.processInstanceId}`,
      { headers: { 'Accept': 'application/json' } })
    .then(response => response.json())
    .then((response: Api.Report) => {
      const canvas = viewer.get('canvas');
      const elementRegistry = viewer.get('elementRegistry');

      response.elements.forEach(element => {
        const shape = elementRegistry.get(element.id);
        if (shape.type === 'bpmn:SequenceFlow') {
          
          canvas.addMarker(element.id, 'piviz-flow-completed');
        } else if (element.status === 'RUNNING') {
          canvas.addMarker(shape.id, 'piviz-activity-running');
        } else if (element.status === 'COMPLETED') {
          canvas.addMarker(shape.id, 'piviz-activity-completed');
        }
      });
    })
    .catch(err => {
      console.error(err);
    });

  }
};

export default plugin;
