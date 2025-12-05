feat: initial domain and API setup (inventory + sales workflow)

- Added core domain entities: Product, InventoryItem, SaleTicket, SaleLine
- Implemented JPA repositories for all aggregates
- Implemented InventoryService with product auto-creation and stock management
- Implemented SaleService with ticket creation, line aggregation, and controlled stock deduction on ticket close
- Added global exception handler returning structured JSON errors (400/409)
- Added REST controllers for inventory and sales modules
- Included initial DTOs for requests and responses
- Added DevelopmentRunner for manual domain testing under 'dev' profile
- Confirmed end-to-end workflow:
    * Add stock
    * Create ticket
    * Add lines
    * Close ticket with validation
    * Proper stock update and conflict handling
