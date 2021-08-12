import {
  Component,
  Inject,
  OnInit,
  Optional,
  EventEmitter,
  Output
} from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  FormControl,
  Validators,
  FormArray
} from '@angular/forms';
import {
  MAT_DIALOG_DATA,
  MatDialogRef,
  MatDialog
} from '@angular/material/dialog';

import {
  NodesApiService,
  TranslationService
} from '@alfresco/adf-core';
import {
  ContentNodeSelectorComponentData,
  ContentNodeSelectorComponent
} from '@alfresco/adf-content-services';

import {
  MinimalNodeEntryEntity,
  NodeEntry,
  MinimalNode,
  Node
} from '@alfresco/js-api';
import { Observable, of, Subject } from 'rxjs';
import { Router } from '@angular/router';

import { switchMap, map} from 'rxjs/operators';
import { DigitalSignatureDialogService } from '../../services/digital-signature.dialog.service';
import { lessThanValidator } from '../validators/lessThan.validator';

@Component({
  selector: 'contracts-digital-signature-request-dialog',
  templateUrl: './digital-signature-request.dialog.component.html',
  styleUrls: ['./digital-signature-request.dialog.component.scss']
})
export class DigitalSignatureRequestDialogComponent implements OnInit {
  form: FormGroup;
  public recipients: FormArray;
  documentName: string;
  documentId: string;
  //recipientAction: string = "sign";
  //expireDays: number = 14;

  folder: MinimalNodeEntryEntity = null;

  /** Emitted when the edit/create folder give error for example a folder with same name already exist*/

  @Output()
  error: EventEmitter<any> = new EventEmitter<any>();

  /** Emitted when the request for signature has been made     */
  @Output()
  success: EventEmitter<any> = new EventEmitter<MinimalNodeEntryEntity>();

  createTitle = 'DIGITAL_SIGNATURE.DIALOGS.DOCUSIGN.TITLE_CREATE';
  document: MinimalNode = null;

  //signedDocumentFolder: NodeEntry = null;
  signedDocumentFolder: MinimalNodeEntryEntity = null;
  currentUserSelection: NodeEntry[] = [];
  isSelectSignedDocumentFolderDialogOpen: boolean = false;
  expirePanelOpenState = false;

  defaultExpireDays = 14;
  defaultExpireWarn = 2;
  defaultRemindDays = 2;
  defaultRemindFrequency = 2;

  constructor(
    private formBuilder: FormBuilder,
    private dialog: MatDialogRef<DigitalSignatureRequestDialogComponent>,
    private supportingDocumentsDialog: MatDialog,
    private nodesApi: NodesApiService,
    private translation: TranslationService,
    private router: Router,
    private digitalSignatureDialogService: DigitalSignatureDialogService,

    @Optional()
    @Inject(MAT_DIALOG_DATA)
    public data: any
  ) {
    console.log('Entered digital-signature-dialog-component with data', data);
    if (data) {
      this.document = data.document || this.document;
      // unless changed the initial value of the taret folder is the same folder that the document is in.

      this.form = this.formBuilder.group(
        {
          recipients: this.formBuilder.array(
            [this.createRecipient()],
            [validateSigner]
          ),
          title: [
            this.translation.instant(
              'DIGITAL_SIGNATURE.DIALOGS.DOCUSIGN.TITLE_DEFAULT_TEXT'
            ),
            [Validators.maxLength(100)]
          ],
          message: [''],
          expireDays: [
            this.defaultExpireDays,
            [Validators.min(0), Validators.max(90)]
          ],
          expireWarn: [
            this.defaultExpireWarn,
            [Validators.min(0), Validators.max(21)]
          ],
          remindDays: [
            this.defaultRemindDays,
            [Validators.min(0), Validators.max(89)]
          ],
          remindFrequency: [
            this.defaultRemindFrequency,
            [Validators.min(0), Validators.max(21)]
          ],
          signeeEmail: ['', [Validators.email]],
          //signeeEmail: [],
          signedDocumentFolder: [],
          signedDocumentFolderName: [],
          //supportingDocuments: [],
          signingRequestTitle: [this.createTitle]
        },
        {
          // expire warn must be les than expire days
          //remind days must be less thatn remind Days
          validators: [
            lessThanValidator('expireDays', 'expireWarn'),
            lessThanValidator('expireDays', 'remindDays')
          ] // <-----
        }
      );
    }
  }

  createRecipient(): FormGroup {
    console.log('Create recipient called');
    return this.formBuilder.group({
      name: new FormControl('', [Validators.required]),
      email: new FormControl('', [Validators.required, Validators.email]),
      action: new FormControl('sign', [Validators.required])
    });
  }

  addRecipient(): void {
    console.log('Add Recipient Called');
    this.recipients = this.form.get('recipients') as FormArray;
    this.recipients.push(this.createRecipient());

    //console.log('Recipients', this.recipients);
  }

  // get the addresses from the form
  get recipientControls() {
    return this.form.get('recipients')['controls'];
  }

  removeRecipient(i: number) {
    this.recipients.removeAt(i);
  }

  ngOnInit() {
    /**console.log('DS route:' + this.route);

    this.route.params.pipe(take(1)).subscribe(params => {
      console.log('ngOnInit ', params);
      const id = params.nodeId;
    }); */

    const { document } = this.data;

    //let signedDocumentFolderName = '';
    let signedDocumentFolderName =
      //document.path.elements[document.path.elements.length].name;
      document.path;
    let signedDocumentFolder = null;

    console.log('signedDocumentFolderName', signedDocumentFolderName);

    if (document) {

      console.log('Document to sign', document);
      this.documentId = document.id || '';
      this.documentName = document.name || '';

      console.log('parentId', document.parentId);

      this.nodesApi
        .getNode(document.parentId)
        .pipe(
          map((entry: MinimalNodeEntryEntity) => {
            console.log('SignedDocumentFolder node', entry);
            //return entry;
            signedDocumentFolder = entry;
            this.form.controls['signedDocumentFolderName'].setValue(
              document.path.name
            );
            this.signedDocumentFolder = signedDocumentFolder;
          })
        )
        .subscribe();
    }
  }
  get editing(): boolean {
    return !!this.data.document;
  }

  get name(): string {
    let { name } = this.form.value;
    return (name || '').trim();
  }

  get expireDays(): string {
    let { expireDays } = this.form.value;
    return expireDays.toString();
  }

  get expireWarn(): string {
    let { expireWarn } = this.form.value;
    return expireWarn.toString();
  }

  get remindDays(): string {
    let { remindDays } = this.form.value;
    return remindDays.toString();
  }

  get remindFrequency(): string {
    let { remindFrequency } = this.form.value;
    return remindFrequency.toString();
  }

  get message(): string {
    let { message } = this.form.value;
    return (message || '').trim();
  }

  get title(): string {
    let { title } = this.form.value;
    return (title || '').trim();
  }

  get recipientsArray(): any[] {
    let { recipients } = this.form.value;
    console.log('Recipients value', recipients);
    return recipients;
  }

  get signeeName(): string {
    let { signeeName } = this.form.value;
    return signeeName || null;
  }

  get signeeEmail(): string {
    let { signeeEmail } = this.form.value;
    return (signeeEmail || '').trim();
  }

  private get properties(): any {
    //const { supplierName: supplierName, supplierEmail, supplierAddress, supplierPhone, supplierContactName } = this;
    const {
      documentName,
      title,
      message,
      expireDays,
      expireWarn,
      remindDays,
      remindFrequency,
      recipientsArray
    } = this;

    return {
      documentName: documentName,
      title: title,
      message: message,
      expireDays: expireDays,
      expireWarn: expireWarn,
      remindDays: remindDays,
      remindFrequency: remindFrequency,
      recipients: recipientsArray,
      targetFolderId: this.signedDocumentFolder.id,
      documentId: this.documentId
    };
  }

  private create(): Observable<any> {

    const { properties } = this;

    const payload = properties;

    console.log('Dialog Signature Dialog payload: ');
    console.log(payload);

    //return this.store.select(fromStore.getAllEventsSelector);
    return of(payload);
  }

  selectSignedFolderLocation() {
    console.log('Entered selectSignedFolderLocation Method');
    this.isSelectSignedDocumentFolderDialogOpen = true;
    //this.digitalSignatureService
    this.digitalSignatureDialogService
      .openSelectFolderDialog(
        this.document.parentId,
        'Select Destination for Signed Document',
        undefined,
        undefined
      )
      .pipe(
        switchMap((selectedValues: any) => {
          console.log('selectSignedFolderLocation', selectedValues);

          const targetFolder = selectedValues[0];

          if (targetFolder) {
            this.form.controls['signedDocumentFolderName'].setValue(
              targetFolder.path.name + '/' + targetFolder.name
            );
            this.signedDocumentFolder = targetFolder;
          }
          this.isSelectSignedDocumentFolderDialogOpen = false;
          return of(targetFolder);
        })
      )
      .subscribe();
  }

  close() {
    console.log('close called');
    this.data.select.complete();
  }

  selectSupportingDocuments() {
    console.log('Entered selectSupportingDocuments Method');

    const data: ContentNodeSelectorComponentData = {
      title: 'Choose an item',
      actionName: 'Choose',
      //selectionMode: "multiple",
      currentFolderId: this.document.parentId,
      select: new Subject<Node[]>()
    };

    let dialogRef = this.supportingDocumentsDialog.open(
      ContentNodeSelectorComponent,
      {
        data,
        panelClass: 'adf-content-node-selector-dialog',
        width: '630px'
        //restoreFocus: true
      }
    );

    data.select.subscribe(
      (selections: Node[]) => {
        // Use or store selection...
        console.log('Selected supporting docs: ', selections);
      },
      error => {
        //your error handling
        console.log("DigitalSignatureRequestDialog error",error)
      },
      () => {
        //action called when an action or cancel is clicked on the dialog
        dialogRef.close();
      }
    );
  }

  submit() {
    const { form, dialog} = this;

    if (form.invalid) {
      console.log('Form Invalid', form);
      return;
    }

    console.log('Submitting values');
    this.create().subscribe(
      (folder: MinimalNodeEntryEntity) => {
        console.log('Closing Digital Signature Request dialog');
        if (folder && !this.isSelectSignedDocumentFolderDialogOpen) {
          dialog.close(folder);
          console.log('Digital Signature Request dialog closed');
        }

        this.success.emit(folder);
        this.data.select.next(folder);
        this.data.select.complete();

      },
      error => {
        console.log(error);
        this.handleError(error);
      }
    );

    console.log('Called Create on Digital Signature Dialog ');
  }

  handleError(error: any): any {
    let errorMessage = 'CORE.MESSAGES.ERRORS.GENERIC';

    try {
      const {
        error: { statusCode }
      } = JSON.parse(error.message);

      if (statusCode === 409) {
        errorMessage = 'CORE.MESSAGES.ERRORS.EXISTENT_FOLDER';
      }
    } catch (err) {
      /* Do nothing, keep the original message */
    }

    this.error.emit(this.translation.instant(errorMessage));

    return error;
  }

  reloadPage(parentId: string) {
    this.router
      .navigateByUrl('/contracts', { skipLocationChange: false })
      .then(() =>
        this.router.navigate(['/contracts/files', parentId], {
          replaceUrl: true
        })
      );
  }

  refresh(): void {
    window.location.reload();
  }
}
/**
 * Function to validate that at least one signer is set for the request
 */
function validateSigner(formArr: FormArray): object | null {
  var valid = false;

  //console.log("Validate Signer entered", formArr)

  for (let recipientGroup of formArr.controls) {
    //console.log("Validate Signer entered", recipientGroup)
    let action = recipientGroup['controls']['action'].value;
    //console.log("action ", action)
    valid = 'sign' == action ? true : false;
    if (valid) {
      break;
    }
  }

  console.log('Validate Signer valid', valid);

  return valid
    ? null
    : {
        noSigner: true
      };
}
