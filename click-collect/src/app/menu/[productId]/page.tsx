import Link from "next/link";
import { notFound } from "next/navigation";
import { ProductCustomizer } from "@/components/ProductCustomizer";
import { getCatalog } from "@/lib/catalog";

type Props = {
  params: Promise<{ productId: string }>;
};

export default async function ProductPage({ params }: Props) {
  const { productId } = await params;
  const { categories, options } = await getCatalog();
  const product = categories
    .flatMap((c) => c.products)
    .find((p) => p.id === productId);

  if (!product) notFound();

  return (
    <div className="mx-auto max-w-xl space-y-4 pb-16">
      <Link
        href="/menu"
        className="text-sm font-semibold text-[var(--kebaba-bordeaux)] hover:underline"
      >
        ← Retour au menu
      </Link>
      <ProductCustomizer product={product} options={options} />
    </div>
  );
}
