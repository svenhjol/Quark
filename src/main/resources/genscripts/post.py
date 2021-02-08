from jsongen import *

copy([
	('unique/block_model_post.json', 'assets/{modid}/models/block/{name}_post.json'),
	('unique/block_item_post.json', 'assets/{modid}/models/item/{name}_post.json'),
	('unique/blockstate_post.json', 'assets/{modid}/blockstates/{name}_post.json'),

	('unique/recipe_post.json', 'data/{modid}/recipes/building/crafting/{name}_post.json')
])

import update_drop_tables
