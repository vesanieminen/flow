export interface Product {
  name: string;
  version: string;
}

export interface ProductAndMessage {
  message: string;
  product: Product;
}

export const findAll = (element: Element | ShadowRoot | Document, tag: string): Element[] => {
  const lightDom = Array.from(element.querySelectorAll(tag));
  const shadowDom = Array.from(element.querySelectorAll('*'))
    .filter((e) => e.shadowRoot)
    .flatMap((e) => findAll(e.shadowRoot!, tag));
  return [...lightDom, ...shadowDom];
};

const manipulate = (element: Element, productAndMessage: ProductAndMessage) => {
  const overlay = (element as any)._overlayElement;
  if (overlay) {
    if (overlay.shadowRoot) {
      const defaultSlot = overlay.shadowRoot.querySelector('slot:not([name])');
      if (defaultSlot && defaultSlot.assignedElements().length > 0) {
        manipulate(defaultSlot.assignedElements()[0], productAndMessage);
        return;
      }
    }
    manipulate(overlay, productAndMessage);
    return;
  }

  const htmlMessage = `${productAndMessage.message} <p>Component: ${productAndMessage.product.name} ${productAndMessage.product.version}</p>`.replace(
    /https:([^ ]*)/g,
    "<a href='https:$1'>https:$1</a>"
  );

  element.outerHTML = `<no-license style="display: flex; align-items:center;text-align:center;justify-content:center;"><div>${htmlMessage}</div></no-license>`;
};

const orgDefine = window.customElements.define.bind(window.customElements);
const missingLicense: { [key: string]: ProductAndMessage } = {};

customElements.define = (name, constructor, options) => {
  const orgCallback = constructor.prototype.connectedCallback;

  // eslint-disable-next-line func-names
  constructor.prototype.connectedCallback = function () {
    const productInfo = missingLicense[this.tagName.toLowerCase()];
    if (productInfo) {
      setTimeout(() => manipulate(this, productInfo), 1000);
    }
    if (orgCallback) {
      orgCallback.call(this);
    }
  };
  orgDefine(name, constructor, options);
};

export const licenseCheckOk = (data: any) => {
  // eslint-disable-next-line no-console
  console.debug('License check ok for ', data);
};

export const licenseCheckFailed = (data: any) => {
  const tag = data.product.name;
  missingLicense[tag] = data;
  // eslint-disable-next-line no-console
  console.error('License check failed for ', tag);

  findAll(document, tag).forEach((element) => {
    setTimeout(() => manipulate(element, missingLicense[tag]), 1000);
  });
};
