document.addEventListener('DOMContentLoaded', () => {
    fetchCartItems();

    function fetchCartItems() {
        fetch('/api/cart')
            .then(response => response.json())
            .then(data => {
                displayCartItems(data);
                updateTotalPrice(data);
            })
            .catch(error => console.error('Error fetching cart items:', error));
    }

    function displayCartItems(items) {
        const cartItemsContainer = document.getElementById('cart-items');
        cartItemsContainer.innerHTML = '';
        items.forEach(item => {
            const itemElement = document.createElement('div');
            itemElement.className = 'cart-item';
            itemElement.id = `item-${item.movieId}`;
            itemElement.innerHTML = `
                <span class="item-title">${item.title}</span>
                <span class="item-quantity">Quantity: <span class="quantity-value">${item.quantity}</span></span>
                <span class="item-price">Price: $<span class="price-value">${item.price.toFixed(2)}</span></span>
                <button class="increase-btn">Increase</button>
                <button class="decrease-btn">Decrease</button>
                <button class="delete-btn">Delete</button>
            `;
            cartItemsContainer.appendChild(itemElement);

            itemElement.querySelector('.increase-btn').addEventListener('click', () => updateItemQuantity(item.movieId, item.quantity + 1));
            itemElement.querySelector('.decrease-btn').addEventListener('click', () => updateItemQuantity(item.movieId, item.quantity - 1));
            itemElement.querySelector('.delete-btn').addEventListener('click', () => deleteItem(item.movieId));
        });
    }

    function updateItemQuantity(movieId, newQuantity) {
        if (newQuantity < 1) return;
        fetch('/api/cart', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `action=update&item=${movieId}&quantity=${newQuantity}`
        })
        .then(fetchCartItems)
        .catch(error => console.error('Error updating item quantity:', error));
    }

    function deleteItem(movieId) {
        fetch('/api/cart', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `action=delete&item=${movieId}`
        })
        .then(fetchCartItems)
        .catch(error => console.error('Error deleting item:', error));
    }

    function updateTotalPrice(items) {
        const totalPrice = items.reduce((total, item) => total + item.price * item.quantity, 0);
        document.getElementById('total-value').textContent = totalPrice.toFixed(2);
    }
}); 