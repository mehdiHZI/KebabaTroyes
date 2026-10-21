import { CategorySection } from "@/components/CategorySection";
import { getCatalog } from "@/lib/catalog";

export const dynamic = "force-dynamic";

export const metadata = {
  title: "Menu",
};

export default async function MenuPage() {
  const { categories } = await getCatalog();

  return (
    <div className="space-y-10 pb-16">
      <header className="space-y-2">
        <h1 className="font-display text-3xl font-bold uppercase tracking-wide text-[var(--kebaba-bordeaux)]">
          Notre carte
        </h1>
        <p className="text-[var(--kebaba-ink-muted)]">
          Cliquez sur un article pour le personnaliser et l’ajouter au panier.
        </p>
      </header>

      {categories.map((cat) => (
        <CategorySection key={cat.id} name={cat.name} products={cat.products} />
      ))}
    </div>
  );
}
