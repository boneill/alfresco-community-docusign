import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'lib-test-component',
  template: `
    <p>
      Test component works hopefully!
    </p>

 <!--   <aca-page-layout>
  <aca-page-layout-content [scrollable]="true">
    <div class="main-content">

      <ng-container >
        <article >
          <header>{{ 'APP.ABOUT.PLUGINS.TITLE' | translate }}</header>

        </article>
      </ng-container>

    
    </div>
  </aca-page-layout-content>
</aca-page-layout>
-->
    
  `,
  styles: [
  ]
})
export class TestComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

}
