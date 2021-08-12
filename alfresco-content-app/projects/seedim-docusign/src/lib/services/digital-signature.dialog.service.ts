import { Injectable } from '@angular/core';

import {
  AppStore,
  SnackbarErrorAction,
  ReloadDocumentListAction,
  SnackbarInfoAction
} from '@alfresco/aca-shared/store';

//import { MatDialog, MatDialogRef} from '@angular/material';
import { Subject, of } from 'rxjs';
import { MinimalNode } from '@alfresco/js-api';
import { Node } from '@alfresco/js-api';

import { DigitalSignatureRequestDialogComponent } from '../dialogs/digital-signature-request.dialog/digital-signature-request.dialog.component';
import { DigitalSignatureService } from './digital-signature.service';
import {
  ContentNodeSelectorComponent,
  ContentNodeSelectorComponentData
} from '@alfresco/adf-content-services';
import { catchError } from 'rxjs/operators';
import { Store } from '@ngrx/store';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';

@Injectable({
  providedIn: 'root'
})
export class DigitalSignatureDialogService {
  constructor(
    private dialog: MatDialog,
    private folderSelectDialog: MatDialog,
    public digitalSignatureRequestDialogRef: MatDialogRef<
      DigitalSignatureRequestDialogComponent
    >,
    public selectFolderDialogRef: MatDialogRef<ContentNodeSelectorComponent>,
    public dialogRef: MatDialog,
    private store: Store<AppStore>,
    public digitalSignatureService: DigitalSignatureService
  ) {}

  /**
   * Open a Digital Signature Dialog
   *
   * On submit it will send a request for a digital signature
   *
   */
  public openDigitalSignatureRequestDialog(document: MinimalNode) {
    console.log('Entered openDigitalSignatureRequestDialog method');

    console.log(
      'Entered digitalSignatureDialogService openDigitalSignatureRequestDialog method'
    );

    const data: any = {
      parentNodeId: document.parentId,
      document: document,
      select: new Subject<Node[]>()
    };

    this.digitalSignatureRequestDialogRef = this.dialog.open(
      DigitalSignatureRequestDialogComponent,
      {
        data,
        width: '800px',
        height: '770px'
      }
    );

    data.select.subscribe(
      formValues => {
        console.log(
          'Success output from openDigitalSignatureRequest method ',
          formValues
        );

        this.digitalSignatureService
          .signatureRequest(formValues)
          .pipe(
            catchError(_ => {
              this.store.dispatch(
                new SnackbarErrorAction(
                  'DIGITAL_SIGNATURE.ACTIONS.ERRORS.SIGNATURE_REQUEST_ERROR'
                )
              );
              return of(null);
            })
          )
          .subscribe(
            (response: any) => {
              console.log('Digital Signaure Request succeeded', response);
              this.store.dispatch(
                new SnackbarInfoAction(
                  'DIGITAL_SIGNATURE.ACTIONS.REQUEST_SIGNATURE_SUCCESS'
                )
              );
              this.store.dispatch(new ReloadDocumentListAction());
            },
            err => console.log('HTTP Error', err),
            () => console.log('Signature request completed.')
          );

        console.log('Signature request sent');
      },
      error => {
        console.log(
          'Error in dialog: openDigitalSignatureRequest method ',
          error
        );
      },
      () => {
        //action called when an action or cancel is clicked on the dialog

        console.log('close dialog: openDigitalSignatureRequest method ');
        this.digitalSignatureRequestDialogRef.close();
      }
    );
  }

  /**
   * Open a content nodel selector dialog
   */
  openSelectFolderDialog(
    startFolderId: string,
    title: string,
    imageResolver?: any,
    _where?: string,
    rowFilter?: any
  ): Subject<Node[]> {
    console.log(
      'Entered digitalSignatureDialogService openSelectFolderDialog method'
    );

    const data: ContentNodeSelectorComponentData = {
      title: title,
      actionName: undefined,
      currentFolderId: startFolderId,
      dropdownHideMyFiles: false,
      dropdownSiteList: undefined,
      rowFilter: rowFilter,
      where: '(isFolder=true)',
      imageResolver: imageResolver,
      isSelectionValid: (entry: Node) => entry.isFolder,
      breadcrumbTransform: undefined,
      excludeSiteContent: undefined,
      select: new Subject<Node[]>(),
      showSearch: true,
      showFilesInResult: false,
      showDropdownSiteList: true
    };

    this.selectFolderDialogRef = this.folderSelectDialog.open(
      ContentNodeSelectorComponent,
      {
        data,
        panelClass: 'adf-content-node-selector-dialog',
        width: '630px',
        restoreFocus: true
      }
    );

    data.select.subscribe(
      (selections: Node[]) => {
        // Use or store selection...
        console.log("Nodes selected from digital signature dialog", selections)
      },
      error => {
        //your error handling
        console.log("Error thrown from digital signature dialog", error)
      },
      () => {
        //action called when an action or cancel is clicked on the dialog
        this.selectFolderDialogRef.close();
      }
    );

    return data.select;
  }
}
