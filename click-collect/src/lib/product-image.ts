/** Photos servies depuis /public/products/{productId}.jpeg */

export function productImageSrc(productId: string): string {
  return `/products/${productId}.jpeg`;
}

export const BRAND_BANNER = "/brand/banner.jpeg";
