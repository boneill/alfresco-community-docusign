import { Routes } from '@angular/router';
import { AuthGuardEcm } from '@alfresco/adf-core';
import { DocusignViewerComponent } from './components/docusign-viewer/docusign-viewer.component';
import { ExtensionsDataLoaderGuard } from '@alfresco/aca-shared';

export const DOCUSIGN_ROUTES: Routes = [
  {
    path: 'docusign/:requestParent/:requestDoc/:preview/:docId',
    component: DocusignViewerComponent,
    canActivate: [AuthGuardEcm, ExtensionsDataLoaderGuard],
    data: {
      title: 'Docusign Viewer'
    }
  }
];
