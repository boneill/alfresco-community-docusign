import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import * as digitalSignatureActions from '../actions/digital-signature.action';
import { map, take } from 'rxjs/operators';
import { Store } from '@ngrx/store';
import {
  AppStore,
  getAppSelection
} from '@alfresco/aca-shared/store';
import { DigitalSignatureDialogService } from '../services/digital-signature.dialog.service';
import { DigitalSignatureService } from '../services/digital-signature.service';
import { Router } from '@angular/router';

@Injectable()
export class DigitalSignatureEffects {
  constructor(
    private actions$: Actions,
    private acaStore: Store<AppStore>,
    private digitalSignatureDialogService: DigitalSignatureDialogService,
    private digitalSignatureService: DigitalSignatureService,
    private router: Router
  ) {}

  @Effect({ dispatch: false })
  signDocument$ = this.actions$.pipe(
    ofType(digitalSignatureActions.SIGN_DOCUMENT),
    map((action: digitalSignatureActions.SignDocument) => {
      if (action.payload && action.payload.entry) {
        console.log('SignDocument action payload ', action.payload);
        this.digitalSignatureDialogService.openDigitalSignatureRequestDialog(
          action.payload.entry
        );
      } else {
        this.acaStore
          .select(getAppSelection)
          .pipe(take(1))
          .subscribe(selection => {
            console.log('SignDocument action selection state ', selection);
            if (selection.file && selection.file.entry) {
              this.digitalSignatureDialogService.openDigitalSignatureRequestDialog(
                selection.file.entry
              );
            }
          });
      }
    })
  );
  @Effect({ dispatch: false })
  navigateToSignedDocument$ = this.actions$.pipe(
    ofType(digitalSignatureActions.NAVIGATE_TO_SIGNED_DOCUMENT),
    map((action: digitalSignatureActions.NavigateToSignedDocument) => {
      if (action.payload && action.payload.entry) {
        console.log('NavigateToSignedDocument action payload ', action.payload);
        if (action.payload.entry) {
          this.digitalSignatureService
            .getSigneDocument(action.payload.entry.id)
            .subscribe(signedDoc => {
              //console.log("SignedDoc", signedDoc);
              //let url = 'docusign/' + action.payload.entry.parentId + '/preview/' + signedDoc.id;
              let url = 'docusign/' + action.payload.entry.parentId +'/'+ action.payload.entry.id + '/preview/' + signedDoc.id;
              console.log('view signed document url', url);
              this.router.navigateByUrl(url);
              /**this.acaStore.dispatch(
                new ViewNodeAction(signedDoc.id, { location: this.router.url })
              );**/
            });
        }
      } else {
        this.acaStore
          .select(getAppSelection)
          .pipe(take(1))
          .subscribe(selection => {
            console.log(
              'NavigateToSignedDocument action selection state ',
              selection
            );
            if (selection.file && selection.file.entry) {
              this.digitalSignatureService
                .getSigneDocument(selection.file.entry.id)
                .subscribe(signedDoc => {
                  //console.log("SignedDoc", signedDoc);
                  //let url = 'docusign/' + selection.file.entry.parentId + '/preview/' + signedDoc.id;
                  let url = 'docusign/' + selection.file.entry.parentId +'/'+ selection.file.entry.id + '/preview/' + signedDoc.id;
                  console.log('view signed document url', url);
                  this.router.navigateByUrl(url);
                  /**this.acaStore.dispatch(
                    new ViewNodeAction(signedDoc.id, {
                      location: this.router.url
                    })
                  );**/


                });
            }
          });
      }
    })
  );
  @Effect({ dispatch: false })
  navigateToCertifiedDocument$ = this.actions$.pipe(
    ofType(digitalSignatureActions.NAVIGATE_TO_CERTIFIED_DOCUMENT),
    map((action: digitalSignatureActions.NavigateToCertifiedDocument) => {
      if (action.payload && action.payload.entry) {
        console.log(
          'NavigateToCertifiedDocument action payload ',
          action.payload.entry
        );
        if (action.payload.entry) {
          let certDocId =
            action.payload.entry.properties['docusign:certifiedDocumentNodeId'];
            //let url = 'docusign/' + action.payload.entry.id + '/preview/' + certDocId;
            let url = 'docusign/' + action.payload.entry.parentId + '/' + action.payload.entry.id + '/preview/' + certDocId;
            console.log('view certified document url', url);
            this.router.navigateByUrl(url);
          /**this.acaStore.dispatch(
            new ViewNodeAction(certDocId, { location: this.router.url })
          );**/
        }
      } else {
        //console.log('Doing next bit else');
        this.acaStore
          .select(getAppSelection)
          .pipe(take(1))
          .subscribe(selection => {
            console.log(
              'NavigateToCertifiedDocument action selection state ',
              selection.file.entry
            );
            if (selection.file && selection.file.entry) {
              let certDocId =
                selection.file.entry.properties[
                  'docusign:certificateOfCompletionNodeId'
                ];
              if (certDocId) {
                //let url = 'docusign/' + selection.file.entry.id + '/preview/' + certDocId;
                let url = 'docusign/' + selection.file.entry.parentId + '/' + selection.file.entry.id + '/preview/' + certDocId;
                console.log('view certified document url', url);
                this.router.navigateByUrl(url);

                /**this.acaStore.dispatch(
                  new ViewNodeAction(certDocId, { location: this.router.url })
                );**/
              }
            }
          });
      }
    })
  );
}
