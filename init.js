db = db.getSiblingDB('admin');
db.auth('root', 'root');

db = db.getSiblingDB('storeDB');

db.createCollection('categories');
db.createCollection('products');

const categories = [
    {
        _id: ObjectId(),
        name: "Running"
    },
    {
        _id: ObjectId(),
        name: "Fitness"
    },
    {
        _id: ObjectId(),
        name: "Team Sports"
    },
    {
        _id: ObjectId(),
        name: "Water Sports"
    },
    {
        _id: ObjectId(),
        name: "Winter Sports"
    },
    {
        _id: ObjectId(),
        name: "Outdoor"
    },
    {
        _id: ObjectId(),
        name: "Nutrition"
    },
    {
        _id: ObjectId(),
        name: "Boxing"
    },
    {
        _id: ObjectId(),
        name: "Survival"
    },
    {
        _id: ObjectId(),
        name: "Motorsports"
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
                name: category.name
            });
        }
    }
    return categoryObjects;
}

const productTemplates = [
    {
        name: "Trail Running Shoes",
        description: "High-performance trail running shoes designed for off-road adventures. Featuring aggressive treads for enhanced grip, a cushioned midsole for shock absorption, and a breathable upper for maximum comfort. Ideal for rocky and muddy terrains.",
        priceRange: [90, 220],
        imageUrl: "images/shoe",
        categoryNames: ["Running", "Outdoor"]
    },
    {
        name: "Running Hydration Pack",
        description: "Lightweight hydration backpack designed for long-distance running and races.",
        priceRange: [50, 120],
        imageUrl: "images/backpack",
        categoryNames: ["Running", "Outdoor"]
    },
    {
        name: "Running Headlamp",
        description: "Bright LED headlamp for night running with adjustable beam and long battery life.",
        priceRange: [25, 85],
        imageUrl: "images/lamp",
        categoryNames: ["Running", "Outdoor"]
    },
    {
        name: "Smart Running Watch",
        description: "Track your pace, heart rate, and calories burned with this advanced GPS-enabled running watch. It offers real-time coaching, route mapping, and compatibility with fitness apps for performance analysis.",
        priceRange: [40, 300],
        imageUrl: "images/watch",
        categoryNames: ["Running", "Outdoor"]
    },

    {
        name: "Smart Fitness Scale",
        description: "Wi-Fi enabled scale that measures weight, body fat, muscle mass, and more.",
        priceRange: [60, 150],
        imageUrl: "images/scale",
        categoryNames: ["Fitness"]
    },
    {
        name: "Adjustable Dumbbells",
        description: "A versatile set of dumbbells with adjustable weight plates, allowing for customized strength training. The compact design saves space, making them perfect for home workouts.",
        priceRange: [20, 100],
        imageUrl: "images/dumbbell",
        categoryNames: ["Fitness"]
    },
    {
        name: "Bike",
        description: "High-density foam roller for muscle recovery and myofascial release.",
        priceRange: [200, 550],
        imageUrl: "images/bike",
        categoryNames: ["Fitness", "Outdoor"]
    },

    {
        name: "Basketball",
        description: "A high-quality basketball with a textured grip for superior ball control. Made with durable composite leather, it ensures excellent bounce and handling on both indoor and outdoor courts.",
        priceRange: [15, 45],
        imageUrl: "images/basketball",
        categoryNames: ["Team Sports", "Fitness"]
    },
    {
        name: "Match-Grade Soccer Ball",
        description: "FIFA-approved soccer ball designed for professional matches. Features thermally bonded panels for durability and optimal flight performance.",
        priceRange: [50, 150],
        imageUrl: "images/football",
        categoryNames: ["Team Sports"]
    },

    {
        name: "Volleyball",
        description: "Once in a while a ball comes along that pushed the game of volleyball further than it has gone before. The Molten Flistatec V5M5000 Volleyball is that ball. Made with a revolutionary Flistatec Flight Stability Technology, this ball improves the way air moves around it for increased control, and the addition of three panels on each side of the ball for better visibility and a smoother rotation. The cover features a softer, thicker microfiber layer with a hexagon pattern and nylon wound layer for increased grip and improved control and accuracy with overhand passing.",
        priceRange: [50, 150],
        imageUrl: "images/volleyball",
        categoryNames: ["Team Sports"]
    },
    {
        name: "Snowboard",
        description: "A lightweight yet durable snowboard with flexible bindings for maximum comfort. Designed for freestyle, all-mountain, and backcountry adventures.",
        priceRange: [125, 750],
        imageUrl: "images/snowboard",
        categoryNames: ["Winter Sports", "Outdoor"]
    },
    {
        name: "Ski Goggles",
        description: "Wide-lens ski goggles with anti-fog and UV protection, ensuring clear vision in all weather conditions. Features an adjustable strap for a secure fit.",
        priceRange: [20, 150],
        imageUrl: "images/ski-google",
        categoryNames: ["Winter Sports", "Outdoor"]
    },

    {
        name: "Boxing Gloves",
        description: "Durable leather boxing gloves with reinforced wrist straps for added support and impact absorption. Suitable for both training and competition.",
        priceRange: [40, 120],
        imageUrl: "images/box",
        categoryNames: ["Boxing", "Fitness"]
    },
    {
        name: "Tennis Racket",
        description: "A high-performance carbon-fiber racket designed for optimal power, control, and spin. Features an ergonomic grip and shock absorption technology to reduce strain on the wrist. Ideal for both amateur and professional players.",
        priceRange: [200, 450],
        imageUrl: "images/tennis-racket",
        categoryNames: ["Team Sports", "Outdoor", "Fitness"]
    },
    {
        name: "Tennis Balls (Pack of 3)",
        description: "Pressurized tournament-grade tennis balls with durable felt for consistent bounce and long-lasting performance. Suitable for all court surfaces, including hard, clay, and grass courts. ",
        priceRange: [100, 250],
        imageUrl: "images/tennis-ball",
        categoryNames: ["Team Sports", "Outdoor", "Fitness"]
    },

    {
        name: "Creatine Monohydrate",
        description: "A scientifically proven supplement that enhances strength, power, and endurance. Helps increase muscle mass and reduce fatigue.",
        priceRange: [10, 50],
        imageUrl: "images/creatine",
        categoryNames: ["Nutrition"]
    },
    {
        name: "Electrolyte Tablets",
        description: "A convenient way to replenish essential minerals lost through sweat. Ideal for endurance athletes and outdoor adventurers.",
        priceRange: [10, 30],
        imageUrl: "images/multi",
        categoryNames: ["Nutrition"]
    },
    {
        name: "Full-Tang Survival Knife ",
        description: "A robust, full-tang survival knife made from high-carbon stainless steel for exceptional durability and edge retention. Features a textured, non-slip grip and a sharp, serrated spine for cutting through tough materials. Comes with a sheath that includes a built-in fire starter and sharpening stone.",
        priceRange: [25, 90],
        imageUrl: "images/knife",
        categoryNames: ["Outdoor", "Survival"]
    },

    {
        name: "Full-Face Motorcycle Helmet",
        description: "A high-performance full-face helmet designed for maximum protection and comfort. Features an aerodynamic shell, anti-fog visor, and multi-ventilation system for optimal airflow. The impact-resistant material ensures safety, while the padded interior provides a snug fit for long rides.",
        priceRange: [30, 90],
        imageUrl: "images/helmet",
        categoryNames: ["Outdoor", "Motorsports"]
    },
    {
        name: "Motorcycle Gloves",
        description: "Durable, high-grip gloves made from breathable mesh and leather, reinforced with carbon fiber knuckle guards for impact protection. Designed for enhanced grip, touchscreen compatibility, and all-weather riding.",
        priceRange: [25, 70],
        imageUrl: "images/gloves",
        categoryNames: ["Outdoor", "Motorsports"]
    },
    {
        name: "Professional Ice Hockey Stick",
        description: "A lightweight yet durable carbon-fiber hockey stick designed for precision shooting and powerful slap shots. Features an ergonomic grip and reinforced blade for better puck control and durability.",
        priceRange: [45, 150],
        imageUrl: "images/hokey-stick",
        categoryNames: ["Team Sports", "Winter Sports"]
    },

    {
        name: "Hockey Helmet with Face Shield",
        description: "A certified impact-resistant helmet featuring a full-face shield for maximum protection. Designed with a shock-absorbing liner and adjustable straps for a secure fit.",
        priceRange: [80, 150],
        imageUrl: "images/hokey-helmet",
        categoryNames: ["Team Sports", "Winter Sports"]
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
            orders: getRandomInt(8, 1000),
            description: template.description,
            ratings: {[getRandomInt(3, 100)]: (getRandomInt(3, 100) / 20)},
            imageUrl: template.imageUrl + (i + 1) + '.png',
            categories: productCategories,
            available: getRandomInt(0, 3) === 1,
        };

        newProducts.push(product);
    }
}

db.products.insertMany(newProducts);
