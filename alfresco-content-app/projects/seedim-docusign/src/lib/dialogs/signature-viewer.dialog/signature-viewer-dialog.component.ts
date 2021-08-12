import {
  Component,
  OnInit,
  Optional,
  Inject,
  Output,
  EventEmitter
} from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'signature-viewer-dialog',
  templateUrl: './signature-viewer-dialog.component.html',
  styleUrls: ['./signature-viewer-dialog.component.scss']
})
export class SignatureViewerDialogComponent implements OnInit {
  nodeId: string = null;

  /** Emitted when the select folder give error */

  @Output()
  error: EventEmitter<any> = new EventEmitter<any>();

  /** Emitted when the folder is seleted and then dialog returns      */
  @Output()
  success: EventEmitter<any> = new EventEmitter<any>();

  constructor(
    @Optional()
    @Inject(MAT_DIALOG_DATA)
    public data: any
  ) {
    if (data) {
      console.log('signature-viewer-dialog.compoent', data);
    }
  }
  ngOnInit() {
    console.log('Component SeedimDocusignComponent loaded');
    this.nodeId = this.data.id;
  }
}
