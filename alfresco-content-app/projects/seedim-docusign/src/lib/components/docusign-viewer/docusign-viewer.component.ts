import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject, Observable, from } from 'rxjs';
import { takeUntil, take } from 'rxjs/operators';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { NodeEntry } from '@alfresco/js-api';
import { ContentApiService } from '@alfresco/aca-shared';
import { AppStore, isInfoDrawerOpened, getAppSelection } from '@alfresco/aca-shared/store';
import { Store } from '@ngrx/store';

@Component({
  selector: 'aca-docusign-viewer',
  templateUrl: './docusign-viewer.component.html',
  styleUrls: ['./docusign-viewer.component.scss']
})
export class DocusignViewerComponent implements OnInit, OnDestroy {
  onDestroy$ = new Subject<boolean>();
  docId: string = null;
  parentId: string = null;
  requestParent: string = null;
  requestDoc: string = null;
  isSignedDocument: boolean = false;
  certId:string = null;
  node: any;
  selection:any;
  infoDrawerOpened$: Observable<boolean>;
  showRightSide = false;

  constructor(private route: ActivatedRoute,
    private router: Router,
    private contentApi: ContentApiService,
    private acaStore: Store<AppStore>,) { }

  ngOnInit(): void {


    this.infoDrawerOpened$ = this.acaStore.select(isInfoDrawerOpened);

    from(this.infoDrawerOpened$)
      .pipe(takeUntil(this.onDestroy$))
      .subscribe((val) => {
        this.showRightSide = val;
      });
      this.acaStore
      .select(getAppSelection)
      .pipe(take(1))
      .subscribe(selection => {
        this.selection = selection;
      })

    this.route.params.subscribe(({ requestDoc,requestParent, docId}: Params) => {
      this.docId = docId;
      this.requestParent = requestParent;
      this.requestDoc = requestDoc;

      this.contentApi
        .getNode(this.docId)
        .pipe(takeUntil(this.onDestroy$))
        .subscribe((node: NodeEntry) => {
          //console.log("Docusign node:" , node);
          this.node = node.entry;
          this.parentId = node.entry.parentId;
          let certDocId = this.node.properties[
                  'docusign:certificateOfCompletionNodeId'
                ];
          if (certDocId) {
            console.log("Setting isSignedDocument to true");
            this.isSignedDocument = true;
            this.certId = certDocId;
          }

        });


      console.log('Initialised with ', this.docId, this.parentId, this.certId, this.isSignedDocument);
    });

  }

  goBack(event: any) {
    console.log('Go back called ', event);

    if (!event) {
      // this.store.dispatch(new ViewFileAction(this.emailNode, this.emailParentId));

      let url = 'libraries/' + this.requestParent + '/(viewer:view/' + this.requestDoc + ')';

      console.log('go back url', url);

      //this.router.navigateByUrl(url, { replaceUrl: true });
      //this.router.navigateByUrl(url, { skipLocationChange: true });
      this.router.navigateByUrl(url);
    }
  }

  navigateToCertifiedDocument() {
    if (this.certId) {
      let url = 'docusign/' + this.requestParent + '/' + this.requestDoc + '/preview/' + this.certId;
      console.log('view certified document url', url);
      this.isSignedDocument = false;
      this.router.navigateByUrl(url);

      /**this.acaStore.dispatch(
        new ViewNodeAction(certDocId, { location: this.router.url })
      );**/
    }
  }

  ngOnDestroy() {
    this.onDestroy$.next(true);
    this.onDestroy$.complete();
  }

}
