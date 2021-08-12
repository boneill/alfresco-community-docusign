import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DigitalSignatureRequestDialogComponent } from './digital-signature-request.dialog.component';

describe('DigitalSignatureRequestDialogComponent', () => {
  let component: DigitalSignatureRequestDialogComponent;
  let fixture: ComponentFixture<DigitalSignatureRequestDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DigitalSignatureRequestDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DigitalSignatureRequestDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
