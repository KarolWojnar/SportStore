db = db.getSiblingDB('admin');
db.auth('root', 'root');

db = db.getSiblingDB('storeDB');

db.createCollection('categories');
db.createCollection('products');

const categories = [
    {
        _id: ObjectId(),
        name: "Running",
        description: "Running equipment and apparel"
    },
    {
        _id: ObjectId(),
        name: "Fitness",
        description: "Gym and fitness equipment"
    },
    {
        _id: ObjectId(),
        name: "Team Sports",
        description: "Equipment for team sports like football, basketball, volleyball"
    },
    {
        _id: ObjectId(),
        name: "Water Sports",
        description: "Equipment for swimming, diving, and other water activities"
    },
    {
        _id: ObjectId(),
        name: "Winter Sports",
        description: "Equipment for skiing, snowboarding, and other winter activities"
    },
    {
        _id: ObjectId(),
        name: "Outdoor",
        description: "Hiking, camping, and outdoor gear"
    },
    {
        _id: ObjectId(),
        name: "Nutrition",
        description: "Sports nutrition and supplements"
    }
];

db.categories.insertMany(categories);

function getRandomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

function getCategoryObjects(categoryNames) {
    const existingCategories = db.categories.find().toArray();
    const categoryObjects = [];
    for (const categoryName of categoryNames) {
        const category = existingCategories.find(c => c.name === categoryName);
        if (category) {
            categoryObjects.push({
                _id: category._id,
                name: category.name,
                description: category.description
            });
        }
    }
    return categoryObjects;
}

const productTemplates = [
    {
        name: "Trail Running Shoes",
        description: "Durable trail running shoes with aggressive tread pattern for off-road terrain.",
        priceRange: [90, 220],
        imageUrl: "images/shoe",
        categoryNames: ["Running", "Outdoor"]
    },
    {
        name: "Running Hydration Pack",
        description: "Lightweight hydration backpack designed for long-distance running and races.",
        priceRange: [50, 120],
        imageUrl: "https://example.com/images/hydration-pack.jpg",
        categoryNames: ["Running", "Outdoor"]
    },
    {
        name: "Running Headlamp",
        description: "Bright LED headlamp for night running with adjustable beam and long battery life.",
        priceRange: [25, 85],
        imageUrl: "https://example.com/images/running-headlamp.jpg",
        categoryNames: ["Running", "Outdoor"]
    },

    {
        name: "Smart Fitness Scale",
        description: "Wi-Fi enabled scale that measures weight, body fat, muscle mass, and more.",
        priceRange: [60, 150],
        imageUrl: "https://example.com/images/smart-scale.jpg",
        categoryNames: ["Fitness"]
    },
    {
        name: "Suspension Training System",
        description: "Complete bodyweight training system for full-body workouts anywhere.",
        priceRange: [70, 200],
        imageUrl: "https://example.com/images/suspension-trainer.jpg",
        categoryNames: ["Fitness"]
    },
    {
        name: "Foam Roller",
        description: "High-density foam roller for muscle recovery and myofascial release.",
        priceRange: [20, 55],
        imageUrl: "https://example.com/images/foam-roller.jpg",
        categoryNames: ["Fitness", "Running"]
    },

    {
        name: "Training Agility Ladder",
        description: "Adjustable agility ladder for footwork and coordination drills.",
        priceRange: [15, 45],
        imageUrl: "https://example.com/images/agility-ladder.jpg",
        categoryNames: ["Team Sports", "Fitness"]
    },
    {
        name: "Basketball Hoop System",
        description: "Adjustable height basketball system with heavy-duty backboard and rim.",
        priceRange: [200, 600],
        imageUrl: "https://example.com/images/basketball-hoop.jpg",
        categoryNames: ["Team Sports"]
    },
    {
        name: "Referee Whistle Set",
        description: "Professional referee whistle set with lanyard and carrying case.",
        priceRange: [8, 25],
        imageUrl: "https://example.com/images/referee-whistle.jpg",
        categoryNames: ["Team Sports"]
    },

    {
        name: "Inflatable Stand-Up Paddle Board",
        description: "Stable and durable inflatable SUP with paddle, pump, and carrying bag.",
        priceRange: [250, 800],
        imageUrl: "https://example.com/images/paddle-board.jpg",
        categoryNames: ["Water Sports", "Outdoor"]
    },
    {
        name: "Swimming Training Fins",
        description: "Short-blade swim fins for technique training and speed development.",
        priceRange: [25, 70],
        imageUrl: "https://example.com/images/swim-fins.jpg",
        categoryNames: ["Water Sports"]
    },
    {
        name: "Waterproof Action Camera",
        description: "Compact waterproof camera for capturing underwater adventures.",
        priceRange: [150, 450],
        imageUrl: "https://example.com/images/action-camera.jpg",
        categoryNames: ["Water Sports", "Outdoor"]
    },

    {
        name: "Heated Ski Socks",
        description: "Battery-powered heated socks for extreme cold conditions.",
        priceRange: [40, 120],
        imageUrl: "https://example.com/images/heated-socks.jpg",
        categoryNames: ["Winter Sports"]
    },
    {
        name: "Avalanche Safety Kit",
        description: "Complete avalanche safety kit with beacon, probe, and shovel.",
        priceRange: [200, 450],
        imageUrl: "https://example.com/images/avalanche-kit.jpg",
        categoryNames: ["Winter Sports", "Outdoor"]
    },
    {
        name: "Ski Helmet with Audio",
        description: "Lightweight ski helmet with integrated Bluetooth audio system.",
        priceRange: [100, 250],
        imageUrl: "https://example.com/images/ski-helmet.jpg",
        categoryNames: ["Winter Sports"]
    },

    {
        name: "Portable Solar Charger",
        description: "Foldable solar panel charger for outdoor adventures and emergency power.",
        priceRange: [50, 150],
        imageUrl: "https://example.com/images/solar-charger.jpg",
        categoryNames: ["Outdoor"]
    },
    {
        name: "Water Filtration System",
        description: "Lightweight water filter that removes 99.9% of bacteria and parasites.",
        priceRange: [30, 120],
        imageUrl: "https://example.com/images/water-filter.jpg",
        categoryNames: ["Outdoor", "Water Sports"]
    },
    {
        name: "Multi-tool Pocket Knife",
        description: "Compact multi-tool with 14 functions for outdoor activities.",
        priceRange: [25, 90],
        imageUrl: "https://example.com/images/multi-tool.jpg",
        categoryNames: ["Outdoor"]
    },

    {
        name: "Plant-Based Protein",
        description: "Complete plant-based protein blend with all essential amino acids.",
        priceRange: [30, 90],
        imageUrl: "https://example.com/images/plant-protein.jpg",
        categoryNames: ["Nutrition"]
    },
    {
        name: "Pre-Workout Supplement",
        description: "Advanced pre-workout formula for energy, focus, and pump.",
        priceRange: [25, 70],
        imageUrl: "https://example.com/images/pre-workout.jpg",
        categoryNames: ["Nutrition"]
    },
    {
        name: "Omega-3 Fish Oil",
        description: "High-quality omega-3 fatty acids for heart and joint health.",
        priceRange: [15, 50],
        imageUrl: "https://example.com/images/fish-oil.jpg",
        categoryNames: ["Nutrition"]
    },

    {
        name: "Fitness Tracker",
        description: "Advanced fitness tracker with heart rate monitoring and sleep analysis.",
        priceRange: [80, 250],
        imageUrl: "https://example.com/images/fitness-tracker.jpg",
        categoryNames: ["Fitness", "Running"]
    },
    {
        name: "Smart Running Insoles",
        description: "Sensor-equipped insoles that analyze your running form and provide feedback.",
        priceRange: [100, 300],
        imageUrl: "https://example.com/images/smart-insoles.jpg",
        categoryNames: ["Running", "Fitness"]
    },
    {
        name: "Heart Rate Monitor Chest Strap",
        description: "Accurate chest strap heart rate monitor compatible with most fitness apps.",
        priceRange: [40, 100],
        imageUrl: "https://example.com/images/heart-rate-monitor.jpg",
        categoryNames: ["Fitness", "Running"]
    }
];

const newProducts = [];

for (const template of productTemplates) {
    for (let i = 0; i < 3; i++) {
        const productCategories = getCategoryObjects(template.categoryNames);

        const product = {
            _id: ObjectId(),
            name: template.name + " " + (i + 1),
            price: parseFloat((getRandomInt(template.priceRange[0] * 100, template.priceRange[1] * 100) / 100).toFixed(2)),
            amountLeft: getRandomInt(0, 100),
            description: template.description,
            imageUrl: template.imageUrl + i + '.png',
            categories: productCategories
        };

        newProducts.push(product);
    }
}

db.products.insertMany(newProducts);
