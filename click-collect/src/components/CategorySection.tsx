import { ProductCard } from "./ProductCard";

type Product = {
  id: string;
  name: string;
  priceCents: number;
  description: string | null;
  imageUrl?: string | null;
};

type Props = {
  name: string;
  products: Product[];
};

export function CategorySection({ name, products }: Props) {
  if (products.length === 0) return null;

  return (
    <section className="space-y-4">
      <div className="kebaba-category-title">{name}</div>
      <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
        {products.map((p) => (
          <ProductCard
            key={p.id}
            id={p.id}
            name={p.name}
            priceCents={p.priceCents}
            description={p.description}
            imageUrl={p.imageUrl}
          />
        ))}
      </div>
    </section>
  );
}
