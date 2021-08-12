import { Injectable } from '@angular/core';
import {
  EcmUserService,
  AlfrescoApiService} from '@alfresco/adf-core';
import { Store } from '@ngrx/store';
import { MatDialogRef } from '@angular/material/dialog';

import { MinimalNode } from '@alfresco/js-api';

import { Observable, of, from } from 'rxjs';

import { switchMap, catchError } from 'rxjs/operators';
import { SelectFolderDialogComponent } from '../dialogs/select-folder-dialog/select-folder-dialog.component';


import { AppStore, SnackbarErrorAction } from '@alfresco/aca-shared/store';

@Injectable({
  providedIn: 'root'
})
export class DigitalSignatureService {


  constructor(
    private store: Store<AppStore>,
    private apiService: AlfrescoApiService,
    public ecmUserService: EcmUserService,
    public selectFolderDialogRef: MatDialogRef<SelectFolderDialogComponent>
  ) {}

  singleSigneeSignatureRequest(
    signeeName: string,
    signeeEmail: string,
    documentId: string,
    targetFolderId: string
  ): void {
    console.log('signatureRequest params:', signeeName, signeeEmail, targetFolderId);

    const url =
      'seedim/docusign/sign?signerEmail=' +
      signeeEmail +
      '&signerName=' +
      signeeName +
      '&documentId=' +
      documentId +
      '&targetFolderId=' +
      targetFolderId;

    from(
      this.apiService
        .getInstance()
        .webScript.executeWebScript('POST', url, null, null, null, null)
    )
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
          console.log('Webscript request response', response);
        },
        err => console.log('HTTP Error', err),
        () => console.log('HTTP request completed.')
      );
  }

  signatureRequest(signatureDetails: any): Observable<void> {
    console.log('signatureRequest', signatureDetails);

    const url = 'seedim/docusign/sign/envelope';

    return from(
      this.apiService
        .getInstance()
        .webScript.executeWebScript(
          'POST',
          url,
          null,
          null,
          null,
          JSON.stringify(signatureDetails)
        )
    );
  }

  // (response: any) => {
  //   console.log("Webscript request response", response);
  // }
  // );

  /**
      get the signed document
    */
  public getSigneDocument(sentDocumentId: string): Observable<any> {
    return from(
      this.coreNodeApi.getTargetAssociations(sentDocumentId, {
        include: ['properties'],
        where: "(assocType='docusign:signedDocumentAssoc')"
      })
    ).pipe(
      switchMap(data => {
        console.log('getSigneDocumentId', data);

        if (data.list.entries[0]) {
          const targetNode = data.list.entries[0].entry;

          if (targetNode != null) {
            return of(targetNode);
          } else {
            return of(undefined);
          }
        }else{
          return of(undefined);
        }
      })
    );
  }

  /**
      get the certified document.  Is id should be stored as metadata against the signed document
    
   public getCertifiedDocument(signedDocumentId: string): Observable<any> {
    return from(
      this.coreNodeApi.getTargetAssociations(sentDocumentId, {
        include: ['properties'],
        where: "(assocType='docusign:signedDocumentAssoc')"
      })
    ).pipe(
      switchMap(data => {
        console.log('getSigneDocumentId', data);

        if (data.list.entries[0]) {
          const targetNode = data.list.entries[0].entry;

          if (targetNode != null) {
            return of(targetNode);
          } else {
            return of(undefined);
          }
        }
      })
    );
  }

  */

  public isSupportedMimeType(node: MinimalNode): boolean {
    console.log('isSupportedMimetype');

    var isSupported = false;

    if (node.isFile && node.content) {
      const mimeType: string = node.content.mimeType;
      console.log('isSupportedMimetype: mimetype: ', mimeType);
      switch (mimeType.toLowerCase()) {
        case 'application/pdf':
          isSupported = true;
          break;
        case 'image/png':
          isSupported = true;
          break;
        case 'image/jpeg':
          isSupported = true;
          break;
        case 'image/gif':
          isSupported = true;
          break;
        case 'image/bmp':
          isSupported = true;
          break;
        case 'application/vnd.openxmlformats-officedocument.wordprocessingml.document':
          isSupported = true;
          break;
        case 'application/vnd.openxmlformats-officedocument.presentationml.presentation':
          isSupported = true;
          break;
        case 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet':
          isSupported = true;
          break;
        case 'image/tiff':
          isSupported = true;
          break;
        default:
          isSupported = false;
      }
    }

    console.log('isSupportedMimetype: return: ', isSupported);

    return isSupported;
  }

  private get coreNodeApi() {
    return this.apiService.getInstance().core.nodesApi;
  }
}
