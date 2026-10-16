// Demonstration loader for the React Router data framework.
// In future phases, this can fetch from the Spring Boot backend (/api/menu).
export async function menuLoader() {
  return [
    {
      id: 1,
      name: 'Salmon Avocado Roll',
      description: 'Fresh Atlantic salmon, ripe avocado, seasoned sushi rice.',
      price: '$12.99',
      tag: 'Popular',
    },
    {
      id: 2,
      name: 'Spicy Tuna Crunch',
      description: 'Yellowfin tuna, spicy mayo, cucumber, tempura flakes.',
      price: '$13.49',
      tag: 'Chef Pick',
    },
    {
      id: 3,
      name: 'Green Goddess Veggie Roll',
      description: 'Crisp cucumber, asparagus, avocado, sesame glaze.',
      price: '$10.99',
      tag: 'Vegan',
    },
    {
      id: 4,
      name: 'Rainbow Roll Special',
      description: 'California roll topped with assorted fresh sashimi.',
      price: '$15.99',
      tag: 'Special',
    },
  ]
}
