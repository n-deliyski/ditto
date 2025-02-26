/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
/* eslint-disable require-jsdoc */
import * as Utils from '../utils.js';

class CrudToolbar extends HTMLElement {
  isEditing = false;
  isEditDisabled = false;
  isCreateDisabled = false;
  isDeleteDisabled = false;
  dom = {
    label: null,
    inputIdValue: null,
    buttonEdit: null,
    buttonCreate: null,
    buttonUpdate: null,
    buttonDelete: null,
    buttonCancel: null,
    divRoot: null,
  };

  get idValue() {
    return this.dom.inputIdValue.value;
  }

  set idValue(newValue) {
    this.dom.inputIdValue.value = newValue;
    if (!this.isDeleteDisabled && newValue && newValue !== '') {
      this.dom.buttonDelete.removeAttribute('hidden');
    } else {
      this.dom.buttonDelete.setAttribute('hidden', '');
    }
  }

  get editDisabled() {
    return this.isEditDisabled;
  }

  set createDisabled(newValue) {
    this.isCreateDisabled = newValue;
    this.setButtonState(this.dom.buttonCreate, newValue);
  }

  set deleteDisabled(newValue) {
    this.isDeleteDisabled = newValue;
    this.setButtonState(this.dom.buttonDelete, newValue);
  }

  set editDisabled(newValue) {
    this.isEditDisabled = newValue;
    if (!this.isEditing) {
      this.setButtonState(this.dom.buttonEdit, newValue);
    }
  }

  setButtonState(button, isDisabled) {
    if (isDisabled) {
      button.setAttribute('hidden', '');
    } else {
      button.removeAttribute('hidden');
    }
  }
  get validationElement() {
    return this.dom.inputIdValue;
  }

  constructor() {
    super();
    this.attachShadow({mode: 'open'});
  }

  connectedCallback() {
    this.shadowRoot.append(document.getElementById('templateCrudToolbar').content.cloneNode(true));
    setTimeout(() => {
      Utils.getAllElementsById(this.dom, this.shadowRoot);
      this.dom.buttonEdit.onclick = () => this.toggleEdit(false);
      this.dom.buttonCancel.onclick = () => this.toggleEdit(true);
      this.dom.label.innerText = this.getAttribute('label') || 'Label';
      this.dom.buttonCreate.onclick = this.eventDispatcher('onCreateClick');
      this.dom.buttonUpdate.onclick = this.eventDispatcher('onUpdateClick');
      this.dom.buttonDelete.onclick = this.eventDispatcher('onDeleteClick');
    });
  };

  eventDispatcher(eventName) {
    return () => {
      this.dispatchEvent(new CustomEvent(eventName, {
        composed: true,
      }));
    };
  }

  toggleEdit(isCancel) {
    this.isEditing = !this.isEditing;
    document.getElementById('modalCrudEdit').classList.toggle('editBackground');
    this.dom.divRoot.classList.toggle('editForground');

    if (this.isEditing || this.isEditDisabled) {
      this.dom.buttonEdit.setAttribute('hidden', '');
    } else {
      this.dom.buttonEdit.removeAttribute('hidden');
    }
    this.dom.buttonCancel.toggleAttribute('hidden');

    if (this.isEditing) {
      if (this.dom.inputIdValue.value) {
        this.dom.buttonUpdate.toggleAttribute('hidden');
      } else if (!this.isCreateDisabled) {
        this.dom.buttonCreate.toggleAttribute('hidden');
      }
    } else {
      this.dom.buttonCreate.setAttribute('hidden', '');
      this.dom.buttonUpdate.setAttribute('hidden', '');
    }
    if (this.isEditing || !this.dom.inputIdValue.value) {
      this.dom.buttonDelete.setAttribute('hidden', '');
    }
    const allowIdChange = this.isEditing && (!this.dom.inputIdValue.value || this.hasAttribute('allowIdChange'));
    this.dom.inputIdValue.disabled = !allowIdChange;
    this.dispatchEvent(new CustomEvent('onEditToggle', {
      composed: true,
      detail: {
        isEditing: this.isEditing,
        isCancel: isCancel,
      },
    }));
  }
}

customElements.define('crud-toolbar', CrudToolbar);
