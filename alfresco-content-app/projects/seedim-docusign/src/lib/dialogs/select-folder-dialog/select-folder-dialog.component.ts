import {
  Component,
  Inject,
  OnInit,
  Optional,
  EventEmitter,
  Output
} from '@angular/core';

import { MAT_DIALOG_DATA } from '@angular/material/dialog';

import {
  RowFilter,
  ShareDataRow,
  ImageResolver
} from '@alfresco/adf-content-services';

import { MinimalNodeEntryEntity, NodeEntry } from '@alfresco/js-api';
import {  Subject } from 'rxjs';


@Component({
  selector: 'contracts-select-folder-dialog',
  templateUrl: './select-folder-dialog.component.html',
  styleUrls: ['./select-folder-dialog.component.scss']
})
export class SelectFolderDialogComponent implements OnInit {
  title = 'APP.CONTRACTS.DIALOGS.SELECT_FOLDER.TITLE_CREATE';
  folder: NodeEntry = null;

  folderFilter: RowFilter;

  imageResolver: ImageResolver;

  where: string;

  navigate: boolean = true;

  //startFolder: MinimalNodeEntryEntity = null;
  startFolderId: string = null;

  /** Emitted when the select folder give error */

  @Output()
  error: EventEmitter<any> = new EventEmitter<any>();

  /** Emitted when the folder is seleted and then dialog returns      */
  @Output()
  success: EventEmitter<any> = new EventEmitter<NodeEntry>();

  selectedFolderSubject: Subject<NodeEntry>;

  //currentUserSelection: NodeEntry[] = [];

  constructor(
    /**private dialog: MatDialogRef<SelectFolderDialogComponent>,
    private nodesApi: NodesApiService,
    private translation: TranslationService,
    private authService: AuthenticationService,
    private nodePermissionDialogService: NodePermissionDialogService,
    private router: Router,**/

    @Optional()
    @Inject(MAT_DIALOG_DATA)
    public data: any
  ) {
    console.log('Entered select-folder-dialog-component', data);
    if (data) {
      this.title = data.title || this.title;
      this.startFolderId = data.startFolderId || this.startFolderId;
      this.selectedFolderSubject = data.selectedFolderSubject;

      if (data.rowFilter) {
        this.rowFilter = data.rowFilter;
      }

      if (data.where) {
        this.where = data.where;
      }

      if (data.imageResolver) {
        this.imageResolver = data.imageResolver;
      }

      if (data.navigate != undefined) {
        this.navigate = data.navigate;
        console.log('ngOnInit');
      }
    }
  }

  ngOnInit() {
    // get the start folder and initialise the documentList to start at that folder

    this.folderFilter = this.rowFilter;
  }

  /** action that runs whhen a user selects a folder  */
  selectFolder(selectedFolder: NodeEntry) {
    console.log('selected Folder', selectedFolder);

    if (selectedFolder) {
      //const user = users[0];

      this.folder = selectedFolder;
    }
  }

  /** action that runs whhen a user navigates  */
  folderChange(selectedFolder: NodeEntry) {
    console.log('folderChange', selectedFolder);

    this.folder = null;
  }

  submit() {
    this.success.emit(this.folder);

    console.log('Called Submit on Select Folder Dialog');

    if (this.selectedFolderSubject) {
      this.selectedFolderSubject.next(this.folder);
    }
  }

  changeBreadcrumbPath(node: MinimalNodeEntryEntity) {
    if (node && node.path && node.path.elements) {
      const elements = node.path.elements;
      if (elements.length > 1) {
        if (elements[1].name === 'Sites') {
          elements.splice(0, 5);
        }
      }
    }

    return node;
  }

  public rowFilter(row: ShareDataRow): boolean {
    let node = row.node.entry;

    if (node && node.isFolder) {
      return true;
    }

    return false;
  }
}
