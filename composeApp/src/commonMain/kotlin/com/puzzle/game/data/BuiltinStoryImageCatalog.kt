package com.puzzle.game.data

import androidx.compose.ui.graphics.Color

data class BuiltinStoryImageSet(
    val id: String,
    val title: String,
    val origin: String,
    val ageRange: String,
    val moral: String,
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val pages: List<BuiltinStoryImagePage>
)

data class BuiltinStoryImagePage(
    val id: String,
    val title: String,
    val story: String,
    val prompt: String,
    val assetFile: String
)

object BuiltinStoryImageCatalog {
    val childSafeStyle =
        "儿童绘本插画，温暖明亮，柔和几何童话建筑感，低刺激色彩，清晰主体，适合3-8岁儿童，无文字，无水印，无恐怖元素，1024x1024"

    val stories = listOf(
        story(
            id = "little-red",
            title = "小红帽",
            origin = "格林童话",
            ageRange = "4-7岁",
            moral = "保持警觉，遇到陌生情况要寻求可信任的大人帮助",
            primary = Color(0xFFC8544D),
            secondary = Color(0xFFF3D7C7),
            accent = Color(0xFF76A66A),
            scenes = listOf(
                scene("戴上红帽", "小女孩带着点心篮，准备穿过森林去看望外婆。", "清晨村庄门口，小女孩戴红色斗篷，手提点心篮，远处是通往森林的小路"),
                scene("森林岔路", "她在花丛和小路之间停下，学习观察路线和方向。", "明亮森林岔路，小女孩看向两条路，路边有野花和指向外婆家的小木牌但没有文字"),
                scene("外婆的小屋", "小屋静静亮着灯，她发现屋里有些不对劲。", "森林深处温暖小屋，窗户透光，小女孩站在门口小心观察"),
                scene("勇敢呼救", "她大声呼救，附近的樵夫和邻居听见后赶来。", "小屋门前，小女孩后退呼救，友善大人从林间赶来，画面安全不惊吓"),
                scene("团圆晚餐", "外婆平安无事，大家围坐在桌边分享点心。", "温暖小屋餐桌，外婆和小女孩微笑分享点心，窗外森林安静")
            )
        ),
        story(
            id = "three-pigs",
            title = "三只小猪",
            origin = "英国民间故事",
            ageRange = "3-7岁",
            moral = "认真准备，坚固的计划会保护自己和朋友",
            primary = Color(0xFFE89978),
            secondary = Color(0xFFFFE3C8),
            accent = Color(0xFF9BBF8A),
            scenes = listOf(
                scene("出发盖房", "三只小猪离开家，各自寻找材料盖房子。", "阳光草地，三只小猪背着小包，旁边有稻草、木头和砖块"),
                scene("稻草小屋", "第一只小猪很快盖好稻草屋，但屋子看起来有些松散。", "柔软稻草小屋，第一只小猪开心挥手，屋顶蓬松"),
                scene("木头小屋", "第二只小猪搭起木屋，比稻草屋更结实。", "森林边木头小屋，第二只小猪整理木板，暖色树木背景"),
                scene("砖头小屋", "第三只小猪一块一块砌砖，耐心完成坚固的家。", "红砖小屋建设中，第三只小猪认真砌砖，工具摆放整齐"),
                scene("风来了", "大风吹过，朋友们躲进最坚固的砖屋里。", "安全的砖屋内，三只小猪靠窗看外面的风，屋里温暖明亮"),
                scene("一起修房", "风停后，三只小猪一起把每座房子修得更牢。", "晴天草地，三只小猪合作修房，稻草木头砖块整齐摆放")
            )
        ),
        story(
            id = "cinderella",
            title = "灰姑娘",
            origin = "欧洲经典童话",
            ageRange = "5-8岁",
            moral = "善良和坚持会让人找到自己的光",
            primary = Color(0xFF8AA7D6),
            secondary = Color(0xFFE7ECF8),
            accent = Color(0xFFD6B46D),
            scenes = listOf(
                scene("炉边愿望", "女孩在炉边整理家务，仍然认真照顾小动物朋友。", "温暖厨房炉边，女孩穿朴素围裙，给小鸟和小鼠放面包屑"),
                scene("魔法南瓜车", "善意带来帮助，南瓜车在星光中出现。", "庭院星光下，南瓜变成马车，女孩惊喜看着，光芒柔和"),
                scene("舞会大厅", "她来到明亮大厅，勇敢走进陌生的人群。", "华丽但柔和的宫殿大厅，女孩穿蓝色礼服站在台阶上，灯光温暖"),
                scene("午夜钟声", "钟声提醒她守住约定，她匆忙离开。", "月光台阶，女孩轻轻提裙奔下楼梯，一只水晶鞋留在台阶上"),
                scene("找到鞋子", "鞋子证明了她真实的经历，也让她重新被看见。", "家门口，女孩试穿水晶鞋，周围人惊讶但友善"),
                scene("新的生活", "她带着善良和勇气，走向更自由的生活。", "晨光花园，女孩和朋友们在喷泉旁微笑，远处宫殿柔和")
            )
        ),
        story(
            id = "snow-white",
            title = "白雪公主",
            origin = "格林童话",
            ageRange = "5-8岁",
            moral = "善良需要智慧守护，朋友能带来力量",
            primary = Color(0xFFE06F72),
            secondary = Color(0xFFF4E6E1),
            accent = Color(0xFF4C7A9B),
            scenes = listOf(
                scene("森林迷路", "女孩走进森林，在害怕时学着寻找安全的地方。", "清晨森林小路，女孩穿经典蓝红配色衣裙，停下观察树间光线"),
                scene("小屋灯光", "她发现一间整洁小屋，决定先休息并整理房间。", "森林小屋室内，小床和小桌整齐排列，女孩轻轻打扫"),
                scene("七个朋友", "七个小矮人回家，大家互相介绍并成为朋友。", "温暖小屋餐桌，七个小矮人围坐，女孩微笑交流"),
                scene("红苹果", "陌生人送来鲜红苹果，朋友们提醒她要小心。", "小屋窗边，女孩看着红苹果，窗外有神秘身影但不恐怖"),
                scene("森林守护", "朋友们守在她身边，等待温柔的帮助到来。", "玻璃花床旁，森林朋友和小矮人安静守护，光线柔和"),
                scene("醒来的早晨", "她醒来后明白，真正的家是彼此守护的地方。", "晨光森林，小矮人和女孩在花丛中庆祝，远处小屋明亮")
            )
        ),
        story(
            id = "sleeping-beauty",
            title = "睡美人",
            origin = "欧洲经典童话",
            ageRange = "5-8岁",
            moral = "耐心、守护和希望能穿过漫长等待",
            primary = Color(0xFFD889B5),
            secondary = Color(0xFFF3E1EF),
            accent = Color(0xFF6DAA9C),
            scenes = listOf(
                scene("祝福大厅", "小公主出生，大家送上善良、智慧和勇气的祝福。", "宫殿大厅，摇篮旁有温柔祝福者和彩色花环"),
                scene("纺锤塔房", "长大后的公主在塔房看见陌生纺锤。", "高塔房间，年轻公主看着纺车，阳光从圆窗照进来"),
                scene("沉睡城堡", "城堡在玫瑰藤中安静沉睡，时间慢慢流过。", "玫瑰藤环绕的城堡，守卫和花园安静沉睡，画面平和"),
                scene("穿过玫瑰", "勇敢的来访者沿着玫瑰小径寻找城门。", "玫瑰拱门小径，年轻旅人走向城堡，藤蔓自然分开"),
                scene("醒来的光", "温柔的问候唤醒公主，也唤醒整座城。", "塔房里金色晨光照亮公主，窗外城堡渐渐醒来"),
                scene("花园庆典", "大家在花园庆祝新的清晨和新的希望。", "城堡花园庆典，玫瑰开放，人们微笑跳舞，无文字")
            )
        ),
        story(
            id = "hansel-gretel",
            title = "糖果屋",
            origin = "格林童话",
            ageRange = "5-8岁",
            moral = "兄妹合作、冷静观察能帮助走出困境",
            primary = Color(0xFFD28E56),
            secondary = Color(0xFFF7DFBE),
            accent = Color(0xFFB65F78),
            scenes = listOf(
                scene("面包屑小路", "兄妹在森林里留下小小线索，努力记住回家的方向。", "森林小路，哥哥和妹妹撒下面包屑，阳光穿过树叶"),
                scene("迷失森林", "小鸟吃掉面包屑，他们发现需要想别的办法。", "傍晚森林，孩子们看着小鸟飞走，表情担心但不惊吓"),
                scene("糖果小屋", "他们找到漂亮小屋，却也学会不能只看外表。", "彩色糖果小屋，孩子们站在门前观察，房子甜美但不过分诱惑"),
                scene("互相鼓励", "在困难里，兄妹互相提醒要冷静。", "小屋厨房旁，兄妹握手商量办法，暖光安全表达"),
                scene("找到宝石", "他们发现能帮助家里的小宝石，准备离开森林。", "小屋角落，小袋子里有彩色宝石，兄妹小心收好"),
                scene("回到家", "他们沿着星光和记忆找到回家的路。", "清晨森林边，兄妹走向家门，家人迎接，画面温暖")
            )
        ),
        story(
            id = "ugly-duckling",
            title = "丑小鸭",
            origin = "安徒生童话",
            ageRange = "4-8岁",
            moral = "每个人都有自己的成长节奏和独特美丽",
            primary = Color(0xFF88A9BF),
            secondary = Color(0xFFE3EEF1),
            accent = Color(0xFFD9A55E),
            scenes = listOf(
                scene("破壳时刻", "最后一只小鸭破壳而出，看起来和大家不一样。", "池塘边巢里，小鸭破壳，其他小鸭围看，芦苇和水面柔和"),
                scene("独自游水", "它在池塘里练习游泳，发现自己其实很努力。", "宽阔池塘，小鸭独自游过水面，倒影清晰，远处鸭群"),
                scene("冬天避风", "寒冷冬天里，它寻找可以停靠的温暖角落。", "冬日湖边，小鸭在芦苇旁避风，雪很轻柔不压抑"),
                scene("看见天鹅", "春天到来，它第一次看见优雅的白天鹅。", "春天湖面，远处天鹅飞过，小鸭抬头望向天空"),
                scene("水中倒影", "它低头看见倒影，发现自己已经长成天鹅。", "清澈湖面，白天鹅低头看倒影，花瓣漂在水上"),
                scene("新的伙伴", "它和新伙伴一起飞向明亮的湖面。", "金色晨光中，几只天鹅飞过湖面，水面开满花")
            )
        ),
        story(
            id = "little-mermaid",
            title = "小美人鱼",
            origin = "安徒生童话",
            ageRange = "6-9岁",
            moral = "向往远方时，也要理解选择和代价",
            primary = Color(0xFF4FA6B8),
            secondary = Color(0xFFD8F0EF),
            accent = Color(0xFFD7A463),
            scenes = listOf(
                scene("海底花园", "小美人鱼在海底花园听姐姐们讲岸上的世界。", "海底花园，美人鱼女孩坐在珊瑚旁，姐姐们和发光水草环绕"),
                scene("浮上海面", "她第一次浮上海面，看见星光和远处船灯。", "夜晚海面，美人鱼从水中探出，远处船灯和星空"),
                scene("暴风过后", "风浪后，她把落水的年轻人带到安全岸边。", "黎明海岸，美人鱼把昏睡年轻人送到沙滩旁，画面安全平和"),
                scene("岸上花园", "她来到岸上，学习用新的方式观察世界。", "宫殿花园，美人鱼女孩在人类装束中看花和台阶，动作轻柔"),
                scene("海边选择", "她在海边思考自己的心愿和家人的牵挂。", "月光海边，女孩站在浪花旁，远处海底光点像家"),
                scene("化作晨光", "她把善意留给世界，随着晨光走向更宽广的天空。", "日出海面，金色光点升起，海面平静，远处飞鸟")
            )
        ),
        story(
            id = "puss-boots",
            title = "穿靴子的猫",
            origin = "法国经典童话",
            ageRange = "5-8岁",
            moral = "机智要和善意一起使用",
            primary = Color(0xFFC07A45),
            secondary = Color(0xFFF0D9BD),
            accent = Color(0xFF496D8A),
            scenes = listOf(
                scene("收到靴子", "小猫穿上靴子，决定帮助年轻主人改变生活。", "乡间小屋门口，橘色小猫穿靴子和帽子，年轻主人惊喜看着"),
                scene("献上礼物", "小猫带着礼物去城堡，礼貌地介绍主人。", "城堡门前，小猫双手递上篮子，守卫友善接待"),
                scene("河边计划", "它用聪明办法让主人获得体面的衣服。", "河边草地，小猫指向远处马车，年轻主人在屏风后换上整洁衣服"),
                scene("经过田野", "小猫请田野上的人们说出主人的好名声。", "金色麦田，小猫和农夫交谈，远处马车经过"),
                scene("巨人城堡", "小猫机智面对骄傲的巨人，保护大家安全。", "高大城堡大厅，小猫站在地毯上与巨大影子对话，画面幽默不恐怖"),
                scene("新的家园", "主人和小猫用诚实与勤劳经营新的家。", "阳光城堡花园，小猫、主人和朋友们一起种花")
            )
        ),
        story(
            id = "beanstalk",
            title = "杰克与豆茎",
            origin = "英国民间故事",
            ageRange = "5-8岁",
            moral = "好奇心需要勇气，也需要判断力",
            primary = Color(0xFF6FA66A),
            secondary = Color(0xFFE4EECF),
            accent = Color(0xFFD8A34D),
            scenes = listOf(
                scene("神奇豆子", "杰克得到几颗神奇豆子，心里充满好奇。", "乡间小路，男孩手里拿着发光豆子，旁边是小屋和奶牛"),
                scene("豆茎入云", "一夜之间，巨大的豆茎长到云上。", "清晨小屋旁，绿色豆茎盘旋升入云端，男孩仰头惊讶"),
                scene("云上城堡", "杰克爬上云端，看见一座巨大的城堡。", "云海之上，男孩站在豆叶上看远处城堡，天空明亮"),
                scene("金色竖琴", "他发现会唱歌的金色竖琴，知道这里藏着秘密。", "巨大城堡桌面，金色竖琴发光，男孩躲在杯子后观察"),
                scene("快速下撤", "听见脚步声，他赶紧沿豆茎回到地面。", "豆茎上，男孩向下爬，云层和叶子形成安全动感"),
                scene("新的开始", "杰克学会珍惜机会，也用收获帮助家人生活。", "晴天小屋前，杰克和家人整理花园，远处豆茎变成绿色拱门")
            )
        )
    )

    private fun story(
        id: String,
        title: String,
        origin: String,
        ageRange: String,
        moral: String,
        primary: Color,
        secondary: Color,
        accent: Color,
        scenes: List<StoryScene>
    ): BuiltinStoryImageSet {
        return BuiltinStoryImageSet(
            id = id,
            title = title,
            origin = origin,
            ageRange = ageRange,
            moral = moral,
            primary = primary,
            secondary = secondary,
            accent = accent,
            pages = scenes.mapIndexed { index, scene ->
                val pageIndex = index + 1
                val pageId = "$id-${pageIndex.toString().padStart(2, '0')}"
                BuiltinStoryImagePage(
                    id = pageId,
                    title = scene.title,
                    story = scene.story,
                    prompt = "${scene.prompt}。$childSafeStyle",
                    assetFile = "stories/$id/$pageId.png"
                )
            }
        )
    }

    private fun scene(title: String, story: String, prompt: String): StoryScene {
        return StoryScene(title, story, prompt)
    }

    private data class StoryScene(
        val title: String,
        val story: String,
        val prompt: String
    )
}
