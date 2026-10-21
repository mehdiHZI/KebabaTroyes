"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";

export type CartOption = {
  optionId: string;
  name: string;
  priceCents: number;
  groupId: string;
};

export type CartLine = {
  key: string;
  productId: string;
  name: string;
  unitPriceCents: number;
  quantity: number;
  options: CartOption[];
};

type CartContextValue = {
  lines: CartLine[];
  addLine: (line: Omit<CartLine, "key" | "quantity"> & { quantity?: number }) => void;
  removeLine: (key: string) => void;
  setQuantity: (key: string, quantity: number) => void;
  clear: () => void;
  itemCount: number;
  totalCents: number;
};

const STORAGE_KEY = "kebaba-cart-v1";

const CartContext = createContext<CartContextValue | null>(null);

function lineKey(productId: string, options: CartOption[]): string {
  const opt = [...options]
    .map((o) => o.optionId)
    .sort()
    .join(",");
  return `${productId}::${opt}`;
}

function lineTotal(line: CartLine): number {
  const opts = line.options.reduce((s, o) => s + o.priceCents, 0);
  return (line.unitPriceCents + opts) * line.quantity;
}

export function CartProvider({ children }: { children: ReactNode }) {
  const [lines, setLines] = useState<CartLine[]>([]);
  const [hydrated, setHydrated] = useState(false);

  useEffect(() => {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (raw) setLines(JSON.parse(raw) as CartLine[]);
    } catch {
      /* ignore */
    }
    setHydrated(true);
  }, []);

  useEffect(() => {
    if (!hydrated) return;
    localStorage.setItem(STORAGE_KEY, JSON.stringify(lines));
  }, [lines, hydrated]);

  const addLine = useCallback(
    (input: Omit<CartLine, "key" | "quantity"> & { quantity?: number }) => {
      const key = lineKey(input.productId, input.options);
      const qty = input.quantity ?? 1;
      setLines((prev) => {
        const existing = prev.find((l) => l.key === key);
        if (existing) {
          return prev.map((l) =>
            l.key === key ? { ...l, quantity: l.quantity + qty } : l,
          );
        }
        return [...prev, { ...input, key, quantity: qty }];
      });
    },
    [],
  );

  const removeLine = useCallback((key: string) => {
    setLines((prev) => prev.filter((l) => l.key !== key));
  }, []);

  const setQuantity = useCallback((key: string, quantity: number) => {
    setLines((prev) => {
      if (quantity <= 0) return prev.filter((l) => l.key !== key);
      return prev.map((l) => (l.key === key ? { ...l, quantity } : l));
    });
  }, []);

  const clear = useCallback(() => setLines([]), []);

  const value = useMemo<CartContextValue>(() => {
    const totalCents = lines.reduce((s, l) => s + lineTotal(l), 0);
    const itemCount = lines.reduce((s, l) => s + l.quantity, 0);
    return { lines, addLine, removeLine, setQuantity, clear, itemCount, totalCents };
  }, [lines, addLine, removeLine, setQuantity, clear]);

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export function useCart(): CartContextValue {
  const ctx = useContext(CartContext);
  if (!ctx) throw new Error("useCart must be used within CartProvider");
  return ctx;
}

export { lineTotal };
