
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CoreModule, MaterialModule, TRANSLATION_PROVIDER } from '@alfresco/adf-core';
import { SharedModule, PageLayoutModule } from '@alfresco/aca-shared';


import { SeedimDocusignComponent } from './seedim-docusign.component';
import { ExtensionService, ExtensionsModule, provideExtensionConfig } from '@alfresco/adf-extensions';
import { RecipientColumnComponent } from './components/recipient-column/recipient-column.component';
import { SupplementalDocumentsComponent } from './components/supplemental-documents/supplemental-documents.component';
import { SignatureDashboardComponent } from './containers/signature-dashboard/signature-dashboard.component';
import { DigitalSignatureRequestDialogComponent } from './dialogs/digital-signature-request.dialog/digital-signature-request.dialog.component';
import { SelectFolderDialogComponent } from './dialogs/select-folder-dialog/select-folder-dialog.component';
import { SignatureViewerDialogComponent } from './dialogs/signature-viewer.dialog/signature-viewer-dialog.component';
import { ContentModule } from '@alfresco/adf-content-services';
import { FlexLayoutModule } from '@angular/flex-layout';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { EffectsModule } from '@ngrx/effects';
import { DigitalSignatureEffects } from './effects/digital-signature.effect';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { SignatureSearchConfigurationService } from './containers/signature-dashboard/signature-search-configuration.service';
import { DigitalSignatureDialogService } from './services/digital-signature.dialog.service';
import { DigitalSignatureService } from './services/digital-signature.service';
import * as DigitalSignatureRules from './rules/digital-signature.rules';
import { DocusignViewerComponent } from './components/docusign-viewer/docusign-viewer.component';
import { RouterModule } from '@angular/router';
import { DOCUSIGN_ROUTES } from './seedim-docusign.routes';

//import { TestComponent} from './components/test-component.component';



@NgModule({
  declarations: [SeedimDocusignComponent,SeedimDocusignComponent,
    DigitalSignatureRequestDialogComponent,
    SelectFolderDialogComponent,
    SignatureDashboardComponent,
    SignatureViewerDialogComponent,
    RecipientColumnComponent,
    SupplementalDocumentsComponent,
    DocusignViewerComponent

  /*TestComponent*/],
  imports: [
    /* CommonModule, CoreModule.forChild(), MatTableModule, SharedModule, PageLayoutModule */
    ExtensionsModule,
    FlexLayoutModule,
    CommonModule,
    CoreModule.forChild(),
    ContentModule.forChild(),
    EffectsModule.forFeature([DigitalSignatureEffects]),
    FormsModule,
    ReactiveFormsModule,
    SharedModule,
    PageLayoutModule,
    MaterialModule,
    RouterModule.forChild(DOCUSIGN_ROUTES)



  ],
  exports: [SeedimDocusignComponent,
    DigitalSignatureRequestDialogComponent,
    SelectFolderDialogComponent,
    SignatureDashboardComponent,
    SignatureViewerDialogComponent,
    RecipientColumnComponent
  ],
    entryComponents: [
      SeedimDocusignComponent,
      SignatureDashboardComponent,
      SignatureViewerDialogComponent
    ],
  providers: [
    provideExtensionConfig(['seedim-docusign.plugin.json']),
    DigitalSignatureService,
    DigitalSignatureDialogService,
    SignatureSearchConfigurationService,
    MatDialog,
    { provide: MatDialogRef, useValue: {} },
    {
      provide: TRANSLATION_PROVIDER,
      multi: true,
      useValue: {
        name: 'seedim-docusign-translations',
        source: 'assets/seedim-docusign-translations'
      }
    }
  ]
})
export class SeedimDocusignModule {
  constructor(extensions: ExtensionService) {
//     extensions.setComponents({
//         'my-extension.main.component': SeedimDocusignComponent
//     });

extensions.setComponents({
  'seedim-docusign.main.component': SeedimDocusignComponent,
  'seedim-docusign.signatures.component': SignatureDashboardComponent
});

extensions.setEvaluators({
  'seedim-docusign.canBeSigned': DigitalSignatureRules.canBeSigned,
  'seedim-docusign.isSigned': DigitalSignatureRules.isSigned,
  'seedim-docusign.isSignedDocument': DigitalSignatureRules.isSignedDocument
});
  }
 }
