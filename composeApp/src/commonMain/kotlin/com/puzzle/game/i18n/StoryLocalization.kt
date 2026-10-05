package com.puzzle.game.i18n

import com.puzzle.game.data.BuiltinStoryImageSet
import com.puzzle.game.data.StoryPageData

data class LocalizedStoryMeta(
    val title: String,
    val origin: String,
    val ageRange: String
)

private data class PageText(val title: String, val story: String)

object StoryLocalization {
    fun story(story: BuiltinStoryImageSet, language: AppLanguage): LocalizedStoryMeta {
        val localized = when (language) {
            AppLanguage.English -> EnglishStoryMeta[story.id]
            AppLanguage.Japanese -> JapaneseStoryMeta[story.id]
            AppLanguage.TraditionalChinese -> null
            AppLanguage.SimplifiedChinese -> null
        }
        return localized ?: LocalizedStoryMeta(
            title = if (language == AppLanguage.TraditionalChinese) story.title.toTraditional() else story.title,
            origin = if (language == AppLanguage.TraditionalChinese) story.origin.toTraditional() else story.origin,
            ageRange = if (language == AppLanguage.TraditionalChinese) story.ageRange.replace("岁", "歲") else story.ageRange
        )
    }

    fun page(page: StoryPageData, language: AppLanguage): StoryPageData {
        val localized = when (language) {
            AppLanguage.English -> EnglishPages[page.id]
            AppLanguage.Japanese -> JapanesePages[page.id]
            AppLanguage.TraditionalChinese -> PageText(page.title.toTraditional(), page.story.toTraditional())
            AppLanguage.SimplifiedChinese -> null
        } ?: return page
        val sceneNumber = page.id.substringAfterLast('-').toIntOrNull() ?: 1
        val chapter = when (language) {
            AppLanguage.English -> "Scene $sceneNumber"
            AppLanguage.Japanese -> "第${sceneNumber}幕"
            AppLanguage.TraditionalChinese -> "第${sceneNumber}幕"
            AppLanguage.SimplifiedChinese -> page.chapter
        }
        return page.copy(
            storyTitle = storyTitle(page.storyId, page.storyTitle, language),
            chapter = chapter,
            title = localized.title,
            story = localized.story,
            theme = page.theme.copy(name = localized.title)
        )
    }

    private fun storyTitle(storyId: String, fallback: String, language: AppLanguage): String {
        return when (language) {
            AppLanguage.English -> EnglishStoryMeta[storyId]?.title
            AppLanguage.Japanese -> JapaneseStoryMeta[storyId]?.title
            AppLanguage.TraditionalChinese -> fallback.toTraditional()
            AppLanguage.SimplifiedChinese -> fallback
        } ?: fallback
    }
}

private val EnglishStoryMeta = mapOf(
    "little-red" to LocalizedStoryMeta("Little Red Riding Hood", "Grimms' Fairy Tales", "Ages 4-7"),
    "three-pigs" to LocalizedStoryMeta("The Three Little Pigs", "English Folktale", "Ages 3-7"),
    "cinderella" to LocalizedStoryMeta("Cinderella", "European Fairy Tale", "Ages 5-8"),
    "snow-white" to LocalizedStoryMeta("Snow White", "Grimms' Fairy Tales", "Ages 5-8"),
    "sleeping-beauty" to LocalizedStoryMeta("Sleeping Beauty", "European Fairy Tale", "Ages 5-8"),
    "hansel-gretel" to LocalizedStoryMeta("Hansel and Gretel", "Grimms' Fairy Tales", "Ages 5-8"),
    "ugly-duckling" to LocalizedStoryMeta("The Ugly Duckling", "Andersen's Fairy Tales", "Ages 4-8"),
    "little-mermaid" to LocalizedStoryMeta("The Little Mermaid", "Andersen's Fairy Tales", "Ages 6-9"),
    "puss-boots" to LocalizedStoryMeta("Puss in Boots", "French Fairy Tale", "Ages 5-8"),
    "beanstalk" to LocalizedStoryMeta("Jack and the Beanstalk", "English Folktale", "Ages 5-8")
)

private val JapaneseStoryMeta = mapOf(
    "little-red" to LocalizedStoryMeta("赤ずきん", "グリム童話", "4〜7歳"),
    "three-pigs" to LocalizedStoryMeta("三びきのこぶた", "イギリス民話", "3〜7歳"),
    "cinderella" to LocalizedStoryMeta("シンデレラ", "ヨーロッパ童話", "5〜8歳"),
    "snow-white" to LocalizedStoryMeta("白雪姫", "グリム童話", "5〜8歳"),
    "sleeping-beauty" to LocalizedStoryMeta("眠れる森の美女", "ヨーロッパ童話", "5〜8歳"),
    "hansel-gretel" to LocalizedStoryMeta("ヘンゼルとグレーテル", "グリム童話", "5〜8歳"),
    "ugly-duckling" to LocalizedStoryMeta("みにくいアヒルの子", "アンデルセン童話", "4〜8歳"),
    "little-mermaid" to LocalizedStoryMeta("人魚姫", "アンデルセン童話", "6〜9歳"),
    "puss-boots" to LocalizedStoryMeta("長ぐつをはいた猫", "フランス童話", "5〜8歳"),
    "beanstalk" to LocalizedStoryMeta("ジャックと豆の木", "イギリス民話", "5〜8歳")
)

private val EnglishPages = mapOf(
    "little-red-01" to PageText("The Red Hood", "A girl packs a basket of treats and gets ready to visit Grandma through the forest."),
    "little-red-02" to PageText("The Forest Fork", "She pauses between the flowers and the path, learning to notice routes and directions."),
    "little-red-03" to PageText("Grandma's Cottage", "The cottage glows quietly, but something inside does not feel quite right."),
    "little-red-04" to PageText("A Brave Call", "She calls loudly for help, and nearby neighbors hurry over."),
    "little-red-05" to PageText("Dinner Together", "Grandma is safe, and everyone shares treats around the table."),
    "three-pigs-01" to PageText("Building Homes", "The three little pigs leave home and each chooses materials for a new house."),
    "three-pigs-02" to PageText("The Straw House", "The first pig finishes quickly, but the straw house looks rather loose."),
    "three-pigs-03" to PageText("The Wooden House", "The second pig builds with wood, making a stronger home."),
    "three-pigs-04" to PageText("The Brick House", "The third pig patiently lays every brick and builds a sturdy home."),
    "three-pigs-05" to PageText("The Wind Arrives", "A strong wind blows, and the friends shelter in the strongest brick house."),
    "three-pigs-06" to PageText("Fixing Together", "When the wind stops, the pigs work together to strengthen every house."),
    "cinderella-01" to PageText("A Wish by the Hearth", "The girl finishes her chores and still cares kindly for her animal friends."),
    "cinderella-02" to PageText("The Pumpkin Coach", "Kindness brings help, and a pumpkin coach appears under the stars."),
    "cinderella-03" to PageText("The Ballroom", "She enters the bright hall and bravely joins a room full of strangers."),
    "cinderella-04" to PageText("Midnight Bells", "The bells remind her of her promise, and she hurries away."),
    "cinderella-05" to PageText("The Glass Slipper", "The slipper proves what truly happened and helps others finally see her."),
    "cinderella-06" to PageText("A New Life", "With kindness and courage, she steps toward a freer life."),
    "snow-white-01" to PageText("Lost in the Forest", "The girl enters the forest and looks for a safe place when she feels afraid."),
    "snow-white-02" to PageText("A Lighted Cottage", "She finds a tidy cottage, rests, and helps put the room in order."),
    "snow-white-03" to PageText("Seven Friends", "Seven little friends return home, and everyone introduces themselves."),
    "snow-white-04" to PageText("The Red Apple", "A stranger offers a bright apple, and her friends remind her to be careful."),
    "snow-white-05" to PageText("Forest Guardians", "Her friends stay close and wait for gentle help to arrive."),
    "snow-white-06" to PageText("Morning Awake", "She learns that a true home is a place where people protect one another."),
    "sleeping-beauty-01" to PageText("A Hall of Blessings", "Everyone welcomes the little princess with wishes for kindness, wisdom, and courage."),
    "sleeping-beauty-02" to PageText("The Spinning Room", "The grown princess discovers an unfamiliar spinning wheel in a tower room."),
    "sleeping-beauty-03" to PageText("The Sleeping Castle", "The castle sleeps quietly among the roses as time passes."),
    "sleeping-beauty-04" to PageText("Through the Roses", "A brave visitor follows the rose path toward the castle gate."),
    "sleeping-beauty-05" to PageText("The Waking Light", "A gentle greeting wakes the princess and the whole castle."),
    "sleeping-beauty-06" to PageText("Garden Celebration", "Everyone celebrates a new morning and new hope in the garden."),
    "hansel-gretel-01" to PageText("A Trail of Crumbs", "The siblings leave small clues in the forest and try to remember the way home."),
    "hansel-gretel-02" to PageText("Lost in the Forest", "Birds eat the crumbs, so the children know they need a different plan."),
    "hansel-gretel-03" to PageText("The Candy Cottage", "They find a beautiful cottage and learn that appearances can be misleading."),
    "hansel-gretel-04" to PageText("Encouraging Each Other", "When things are difficult, the siblings remind each other to stay calm."),
    "hansel-gretel-05" to PageText("The Little Gems", "They find gems that can help their family and prepare to leave the forest."),
    "hansel-gretel-06" to PageText("Home Again", "Following starlight and memory, they finally find their way home."),
    "ugly-duckling-01" to PageText("Hatching Day", "The last duckling hatches and looks different from everyone else."),
    "ugly-duckling-02" to PageText("Swimming Alone", "It practices across the pond and discovers how hard it can work."),
    "ugly-duckling-03" to PageText("Shelter in Winter", "During the cold winter, it searches for a warm, sheltered corner."),
    "ugly-duckling-04" to PageText("Seeing Swans", "Spring arrives, and it sees graceful white swans for the first time."),
    "ugly-duckling-05" to PageText("A Reflection", "Looking into the water, it discovers that it has grown into a swan."),
    "ugly-duckling-06" to PageText("New Friends", "It flies with new friends toward a bright lake."),
    "little-mermaid-01" to PageText("The Undersea Garden", "The little mermaid listens to her sisters describe the world above."),
    "little-mermaid-02" to PageText("Above the Waves", "She reaches the surface for the first time and sees stars and distant ship lights."),
    "little-mermaid-03" to PageText("After the Storm", "After the storm, she carries a young traveler safely to shore."),
    "little-mermaid-04" to PageText("The Palace Garden", "On land, she learns new ways to observe the world."),
    "little-mermaid-05" to PageText("A Choice by the Sea", "She thinks about her wish and the family she misses."),
    "little-mermaid-06" to PageText("Into the Morning Light", "She leaves kindness behind and follows the dawn toward a wider sky."),
    "puss-boots-01" to PageText("A Pair of Boots", "The cat puts on his boots and decides to help his young friend."),
    "puss-boots-02" to PageText("A Courteous Gift", "The cat brings a gift to the castle and politely introduces his friend."),
    "puss-boots-03" to PageText("A Plan by the River", "With a clever plan, he helps his friend find fine clothes."),
    "puss-boots-04" to PageText("Across the Fields", "The cat asks the farmers to speak kindly of his friend."),
    "puss-boots-05" to PageText("The Giant's Castle", "The clever cat faces a proud giant and keeps everyone safe."),
    "puss-boots-06" to PageText("A New Home", "The cat and his friend care for their new home with honesty and hard work."),
    "beanstalk-01" to PageText("Magic Beans", "Jack receives a few magic beans and feels wonderfully curious."),
    "beanstalk-02" to PageText("A Beanstalk to the Clouds", "Overnight, a giant beanstalk grows high into the clouds."),
    "beanstalk-03" to PageText("Castle in the Clouds", "Jack climbs up and discovers an enormous castle above the clouds."),
    "beanstalk-04" to PageText("The Golden Harp", "He finds a singing golden harp and realizes the castle holds a secret."),
    "beanstalk-05" to PageText("A Quick Descent", "Hearing footsteps, he quickly climbs back down the beanstalk."),
    "beanstalk-06" to PageText("A New Beginning", "Jack learns to value opportunity and uses what he found to help his family.")
)

private val JapanesePages = mapOf(
    "little-red-01" to PageText("赤いずきん", "女の子はお菓子のかごを持ち、森の向こうのおばあさんを訪ねます。"),
    "little-red-02" to PageText("森の分かれ道", "花畑と小道の間で立ち止まり、道と方向をよく確かめます。"),
    "little-red-03" to PageText("おばあさんの家", "家には明かりがついていますが、いつもと少し様子が違います。"),
    "little-red-04" to PageText("勇気を出して", "大きな声で助けを呼ぶと、近くの大人たちが駆けつけます。"),
    "little-red-05" to PageText("みんなで夕食", "おばあさんは無事で、みんなでお菓子を分け合います。"),
    "three-pigs-01" to PageText("家を建てよう", "三びきのこぶたは家を出て、それぞれ材料を探します。"),
    "three-pigs-02" to PageText("わらの家", "一番目のこぶたはすぐに作りましたが、少し弱そうです。"),
    "three-pigs-03" to PageText("木の家", "二番目のこぶたは木を使い、もっと丈夫な家を建てます。"),
    "three-pigs-04" to PageText("れんがの家", "三番目のこぶたは一つずつ丁寧にれんがを積みます。"),
    "three-pigs-05" to PageText("風が来た", "強い風が吹き、みんなは一番丈夫な家に避難します。"),
    "three-pigs-06" to PageText("いっしょに修理", "風がやむと、三びきで全部の家を丈夫に直します。"),
    "cinderella-01" to PageText("暖炉のそばの願い", "少女は家事をしながら、小さな動物たちにも優しくします。"),
    "cinderella-02" to PageText("かぼちゃの馬車", "優しさに助けられ、星空の下に馬車が現れます。"),
    "cinderella-03" to PageText("舞踏会の広間", "明るい広間へ入り、勇気を出して人々の中へ進みます。"),
    "cinderella-04" to PageText("真夜中の鐘", "約束を知らせる鐘が鳴り、少女は急いで帰ります。"),
    "cinderella-05" to PageText("ガラスの靴", "靴が本当の出来事を伝え、少女は再び見つけてもらえます。"),
    "cinderella-06" to PageText("新しい暮らし", "優しさと勇気を胸に、自由な未来へ歩き出します。"),
    "snow-white-01" to PageText("森で迷って", "少女は怖いときにも、安全な場所を探そうとします。"),
    "snow-white-02" to PageText("小さな家の明かり", "きれいな家を見つけ、休みながら部屋を整えます。"),
    "snow-white-03" to PageText("七人の友だち", "七人の小さな友だちが帰り、みんなで自己紹介します。"),
    "snow-white-04" to PageText("赤いりんご", "知らない人のりんごを見て、友だちの注意を思い出します。"),
    "snow-white-05" to PageText("森の見守り", "友だちはそばで見守り、優しい助けを待ちます。"),
    "snow-white-06" to PageText("目覚めの朝", "本当の家は互いを守る場所だと気づきます。"),
    "sleeping-beauty-01" to PageText("祝福の広間", "小さな姫に、優しさと知恵と勇気の祝福が贈られます。"),
    "sleeping-beauty-02" to PageText("糸車の部屋", "成長した姫は塔の部屋で見知らぬ糸車を見つけます。"),
    "sleeping-beauty-03" to PageText("眠るお城", "ばらに囲まれた城は静かに眠り、時が流れます。"),
    "sleeping-beauty-04" to PageText("ばらの道", "勇気ある旅人がばらの小道を通って城門を目指します。"),
    "sleeping-beauty-05" to PageText("目覚めの光", "優しい呼びかけが姫と城のみんなを目覚めさせます。"),
    "sleeping-beauty-06" to PageText("庭の祝宴", "新しい朝と希望を、みんなで庭で祝います。"),
    "hansel-gretel-01" to PageText("パンくずの道", "兄妹は小さな目印を残し、帰り道を覚えようとします。"),
    "hansel-gretel-02" to PageText("森で迷って", "鳥がパンくずを食べてしまい、別の方法を考えます。"),
    "hansel-gretel-03" to PageText("お菓子の家", "美しい家を見つけ、見た目だけでは分からないと学びます。"),
    "hansel-gretel-04" to PageText("励まし合って", "困ったときも、兄妹は落ち着こうと声をかけ合います。"),
    "hansel-gretel-05" to PageText("小さな宝石", "家族を助けられる宝石を見つけ、森を出る準備をします。"),
    "hansel-gretel-06" to PageText("ただいま", "星の光と記憶を頼りに、家へ帰る道を見つけます。"),
    "ugly-duckling-01" to PageText("たまごから誕生", "最後のひなが生まれ、ほかのみんなとは少し違って見えます。"),
    "ugly-duckling-02" to PageText("ひとりで泳ぐ", "池で泳ぐ練習を続け、自分の頑張りに気づきます。"),
    "ugly-duckling-03" to PageText("冬の風よけ", "寒い冬、暖かく休める場所を探します。"),
    "ugly-duckling-04" to PageText("白鳥との出会い", "春になり、初めて美しい白鳥を見上げます。"),
    "ugly-duckling-05" to PageText("水の中の姿", "水面を見ると、自分も白鳥になったことに気づきます。"),
    "ugly-duckling-06" to PageText("新しい仲間", "新しい仲間といっしょに明るい湖へ飛び立ちます。"),
    "little-mermaid-01" to PageText("海の庭", "人魚姫は姉たちから海の上の世界について聞きます。"),
    "little-mermaid-02" to PageText("海の上へ", "初めて水面に出て、星空と遠い船の明かりを見ます。"),
    "little-mermaid-03" to PageText("嵐のあと", "嵐のあと、若者を安全な岸へ運びます。"),
    "little-mermaid-04" to PageText("宮殿の庭", "陸に上がり、新しい方法で世界を見つめます。"),
    "little-mermaid-05" to PageText("海辺の選択", "自分の願いと、大切な家族のことを考えます。"),
    "little-mermaid-06" to PageText("朝の光へ", "優しさを世界に残し、広い空へ向かいます。"),
    "puss-boots-01" to PageText("長ぐつをはいて", "猫は長ぐつをはき、若い主人を助けると決めます。"),
    "puss-boots-02" to PageText("心をこめた贈り物", "城へ贈り物を運び、礼儀正しく主人を紹介します。"),
    "puss-boots-03" to PageText("川辺の作戦", "賢い作戦で、主人が立派な服を手に入れるのを助けます。"),
    "puss-boots-04" to PageText("畑を通って", "畑の人々に主人の良いところを話してもらいます。"),
    "puss-boots-05" to PageText("巨人の城", "賢い猫は自慢好きの巨人に向き合い、みんなを守ります。"),
    "puss-boots-06" to PageText("新しい家", "猫と主人は正直さと努力で新しい家を大切にします。"),
    "beanstalk-01" to PageText("魔法の豆", "ジャックは魔法の豆を手に入れ、わくわくします。"),
    "beanstalk-02" to PageText("雲まで伸びる豆の木", "一晩で大きな豆の木が雲の上まで伸びます。"),
    "beanstalk-03" to PageText("雲の上の城", "豆の木を登り、雲の上に巨大な城を見つけます。"),
    "beanstalk-04" to PageText("金のハープ", "歌う金のハープを見つけ、城の秘密に気づきます。"),
    "beanstalk-05" to PageText("急いで下へ", "足音を聞き、急いで豆の木を下ります。"),
    "beanstalk-06" to PageText("新しい始まり", "機会を大切にすることを学び、家族の暮らしを助けます。")
)

private fun String.toTraditional(): String {
    val phrases = mapOf(
        "里面" to "裡面", "这里" to "這裡", "那里" to "那裡", "发现" to "發現",
        "准备" to "準備", "帮助" to "幫助", "学习" to "學習", "寻找" to "尋找",
        "经过" to "經過", "看见" to "看見", "远处" to "遠處", "一起" to "一起"
    )
    var result = this
    phrases.forEach { (source, target) -> result = result.replace(source, target) }
    val chars = mapOf(
        '这' to '這', '个' to '個', '们' to '們', '里' to '裡', '发' to '發', '现' to '現',
        '让' to '讓', '还' to '還', '进' to '進', '过' to '過', '学' to '學', '会' to '會',
        '来' to '來', '后' to '後', '为' to '為', '开' to '開', '见' to '見', '从' to '從',
        '对' to '對', '说' to '說', '风' to '風', '远' to '遠', '边' to '邊', '门' to '門',
        '间' to '間', '头' to '頭', '条' to '條', '马' to '馬', '车' to '車', '钟' to '鐘',
        '声' to '聲', '轻' to '輕', '带' to '帶', '着' to '著', '点' to '點', '灯' to '燈',
        '时' to '時', '国' to '國', '经' to '經', '丽' to '麗', '亲' to '親', '实' to '實',
        '宝' to '寶', '贝' to '貝', '树' to '樹', '叶' to '葉', '鸟' to '鳥', '鱼' to '魚',
        '爱' to '愛', '梦' to '夢', '话' to '話', '画' to '畫', '与' to '與', '云' to '雲',
        '当' to '當', '应' to '應', '该' to '該', '团' to '團', '圆' to '圓', '节' to '節',
        '无' to '無', '长' to '長', '东' to '東', '乐' to '樂', '万' to '萬', '广' to '廣',
        '紧' to '緊', '张' to '張', '气' to '氣', '听' to '聽', '写' to '寫', '读' to '讀',
        '难' to '難', '离' to '離', '动' to '動', '静' to '靜', '华' to '華', '险' to '險',
        '护' to '護', '觉' to '覺', '欢' to '歡', '庆' to '慶', '变' to '變', '记' to '記',
        '忆' to '憶', '别' to '別', '种' to '種', '样' to '樣', '毕' to '畢', '业' to '業',
        '岁' to '歲', '块' to '塊', '乡' to '鄉', '农' to '農', '台' to '臺'
    )
    return result.map { chars[it] ?: it }.joinToString("")
}
