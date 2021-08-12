import { Component, OnInit, OnChanges } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {
  MinimalNodeEntryEntity,
  MinimalNodeEntity,
  NodePaging
} from '@alfresco/js-api';
import { ContentApiService } from '@alfresco/aca-shared';

import {
  SearchConfigurationService,
  SearchService,
  AuthenticationService
} from '@alfresco/adf-core';
import { SignatureSearchConfigurationService } from './signature-search-configuration.service';
import { debounceTime } from 'rxjs/operators';

@Component({
  selector: 'signature-dashboard',
  templateUrl: './signature-dashboard.component.html',
  styleUrls: ['./signature-dashboard.component.scss'],
  providers: [
    {
      provide: SearchConfigurationService,
      useClass: SignatureSearchConfigurationService
    },
    SearchService
  ]
})
export class SignatureDashboardComponent implements OnInit, OnChanges {
  resultNodePageList: NodePaging;

  isValidPath = true;
  isSmallScreen = false;
  isAdmin = true;
  
  nodeId: string;
  node: MinimalNodeEntryEntity;
  //showViewer: boolean = false;

  searchTerm: string;
  status = 'sent';
  showSigned: boolean = false;

  signaturesRequestedBy = 'me';
  signatureRequestedByQuery = '';

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthenticationService,
    private contentApi: ContentApiService
  ) {}

  ngOnInit() {
    console.log('Entered SignatureDashboardComponent');
    //this.searchTerm = "ASPECT:'docusign:digitalSignature'";
    this.signatureRequestedByQuery =
      "docusign:sentBy:'" + this.authService.getEcmUsername() + "'";
    this.searchTerm =
      // "ASPECT:'docusign:digitalSignature' AND " +
      "docusign:status:'sent' AND " + this.signatureRequestedByQuery;
    console.log('Init Search Term ' + this.searchTerm);
    //this.signatureRequestedByQuery = "docusign:sentBy:'" + this.authService.getEcmUsername() + "'";
  }

  filterChanged($event) {
    console.log('Status Changed to ', this.status, $event);
    console.log('signatureRequestedBy Changed to', this.signaturesRequestedBy);

    //this.searchTerm = "ASPECT:'docusign:digitalSignature'";
    this.searchTerm = '';
    this.searchTerm = this.searchTerm + "docusign:status:'" + this.status + "'";

    // add the search term for who requested the signature
    if (this.signaturesRequestedBy == 'me') {
      this.searchTerm =
        this.searchTerm + ' AND ' + this.signatureRequestedByQuery;
    }

    if (this.status == 'completed') {
      this.showSigned = true;
    } else {
      this.showSigned = false;
    }

    console.log('filterChanged again and . New Search Term ' + this.searchTerm);
  }

  ngOnChanges(changes) {
    if (changes) {
      console.log(changes);
    }
  }

  showSearchResult($event) {
    console.log('Search Results', $event);
  }

  onSearchResultLoaded(nodePaging: NodePaging) {
    this.resultNodePageList = nodePaging;

    console.log('NodePaging: ', this.resultNodePageList);
    //   this.pagination = nodePaging.list.pagination;
  }

  isViewSignedDisabled = (node): boolean => {
    console.log('isviewSign disabled: ', node);
    if (
      node &&
      node.entry &&
      node.entry.properties &&
      node.entry.properties['docusign:status'] === 'completed'
    ) {
      return false;
    }
    return true;
  };

  navigateToSigned(event) {
    let entry = event.value.entry;

    console.log('Navigate to entry', entry.properties);

    if (entry.properties && entry.properties['docusign:signedDocumentId']) {
      const id = entry.properties['docusign:signedDocumentId'];
      console.log('Id ', id);

      this.contentApi
        .getNode(id)
        .pipe(debounceTime(300))
        .subscribe(signedNode => {
          console.log('signedNode ', signedNode);
          // extras?: ViewNodeExtras
          // this.store.dispatch(new ViewNodeAction(id, extras));

          let url =
            'libraries/' +
            signedNode.entry.parentId +
            '/(viewer:view/' +
            signedNode.entry.id +
            ')';
          console.log('Signature Dashboard navigate url', url);
          this.router.navigateByUrl(url);
        });
    }
  }

  navigate(nodeId: string = null) {
    console.log('navigate to Node', nodeId);
    const commands = ['./files/'];

    if (nodeId && !this.isRootNode(nodeId)) {
      commands.push(nodeId);
    }

    this.router.navigate(commands, {
      relativeTo: this.route.parent
    });
  }

  navigateTo(node: MinimalNodeEntity) {
    if (node && node.entry) {
      const { id, isFolder } = node.entry;

      if (isFolder) {
        this.navigate(id);
        return;
      }
      //this.showPreview(node);
      //this.showPreview(node, { location: this.router.url });
      let url =
        'libraries/' +
        node.entry.parentId +
        '/(viewer:view/' +
        node.entry.id +
        ')';
      console.log('Signature Dashboard navigate url', url);
      this.router.navigateByUrl(url);
    }
  }
  isRootNode(nodeId: string): boolean {
    if (
      this.node &&
      this.node.path &&
      this.node.path.elements &&
      this.node.path.elements.length > 0
    ) {
      return this.node.path.elements[0].id === nodeId;
    }
    return false;
  }
}
