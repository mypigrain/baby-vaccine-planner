package com.vaccineplanner.data.repository

import com.vaccineplanner.data.model.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object VaccineRepository {
    
    val freeVaccines: List<Vaccine> = listOf(
        Vaccine(
            id = "bcg",
            name = "BCG",
            chineseName = "卡介苗",
            description = "预防结核病",
            diseaseInfo = DiseaseInfo(
                name = "结核病",
                description = "结核病是由结核杆菌引起的慢性传染病，可累及全身多个器官，但以肺结核最为常见。",
                incidenceData = listOf(
                    EpidemicData("2022年肺结核发病率", "55.5/10万", "中国疾控中心"),
                    EpidemicData("0-14岁儿童发病率", "约10/10万", "国家统计局")
                ),
                outcomes = DiseaseOutcomes(
                    best = "正规治疗可完全康复，不留后遗症",
                    worst = "可导致肺毁损、呼吸衰竭甚至死亡",
                    typical = "需要6-9个月规范治疗，可治愈但可能留下疤痕"
                )
            ),
            price = 0.0,
            isFree = true,
            doses = 1,
            intervalDays = listOf(0),
            ageRange = "出生时"
        ),
        Vaccine(
            id = "hepb",
            name = "HepB",
            chineseName = "乙肝疫苗",
            description = "预防乙型肝炎",
            diseaseInfo = DiseaseInfo(
                name = "乙型肝炎",
                description = "乙肝是由乙型肝炎病毒引起的肝脏疾病，可导致急慢性肝炎、肝硬化和肝癌。",
                incidenceData = listOf(
                    EpidemicData("乙肝表面抗原携带率", "约5-6%", "中国疾控中心"),
                    EpidemicData("慢性乙肝患者", "约2000-3000万", "WHO")
                ),
                outcomes = DiseaseOutcomes(
                    best = "接种疫苗后可获得终身免疫",
                    worst = "可能发展为肝硬化、肝癌",
                    typical = "急性乙肝可治愈，部分转为慢性携带者"
                ),
                comparisonWithFree = "乙肝疫苗免费，仅需接种3剂，效果与自费疫苗相同"
            ),
            price = 0.0,
            isFree = true,
            doses = 3,
            intervalDays = listOf(0, 30, 180),
            ageRange = "0、1、6月龄"
        ),
        Vaccine(
            id = "ipv",
            name = "IPV",
            chineseName = "脊灰灭活疫苗",
            description = "预防脊髓灰质炎（注射）",
            diseaseInfo = DiseaseInfo(
                name = "脊髓灰质炎",
                description = "是由脊髓灰质炎病毒引起的急性传染病，主要影响5岁以下儿童，可导致永久性瘫痪。",
                incidenceData = listOf(
                    EpidemicData("中国已实现无脊髓灰质炎", "2000年通过认证", "WHO"),
                    EpidemicData("全球野生病毒病例", "2022年仅30例", "WHO")
                ),
                outcomes = DiseaseOutcomes(
                    best = "完全康复",
                    worst = "四肢瘫痪、呼吸肌麻痹导致死亡",
                    typical = "大部分患者留下肌肉萎缩、跛行等后遗症"
                )
            ),
            price = 0.0,
            isFree = true,
            doses = 2,
            intervalDays = listOf(60, 90),
            ageRange = "2、3月龄",
            notes = "2025年新规：2月龄开始接种"
        ),
        Vaccine(
            id = "opv",
            name = "OPV",
            chineseName = "脊灰减毒活疫苗",
            description = "预防脊髓灰质炎（口服）",
            diseaseInfo = DiseaseInfo(
                name = "脊髓灰质炎",
                description = "是由脊髓灰质炎病毒引起的急性传染病，主要影响5岁以下儿童，可导致永久性瘫痪。",
                incidenceData = listOf(
                    EpidemicData("中国已实现无脊髓灰质炎", "2000年通过认证", "WHO"),
                    EpidemicData("全球野生病毒病例", "2022年仅30例", "WHO")
                ),
                outcomes = DiseaseOutcomes(
                    best = "完全康复",
                    worst = "四肢瘫痪、呼吸肌麻痹导致死亡",
                    typical = "大部分患者留下肌肉萎缩、跛行等后遗症"
                )
            ),
            price = 0.0,
            isFree = true,
            doses = 2,
            intervalDays = listOf(120, 1460),
            ageRange = "4月龄、4周岁",
            notes = "口服脊灰，前后半小时禁食"
        ),
        Vaccine(
            id = "dtp",
            name = "DTaP",
            chineseName = "百白破疫苗",
            description = "预防百日咳、白喉、破伤风",
            diseaseInfo = DiseaseInfo(
                name = "百日咳",
                description = "百日咳是由百日咳杆菌引起的急性呼吸道传染病，以阵发性痉挛性咳嗽为主要特征。",
                incidenceData = listOf(
                    EpidemicData("2022年百日咳发病率", "0.8/10万", "国家疾控中心"),
                    EpidemicData("婴儿死亡率", "在未接种疫苗地区可高达4%", "WHO")
                ),
                outcomes = DiseaseOutcomes(
                    best = "完全康复，无后遗症",
                    worst = "6个月以下婴儿可能发生呼吸暂停、肺炎，脑病甚至死亡",
                    typical = "咳嗽持续2-6周，严重影响睡眠和进食"
                )
            ),
            price = 0.0,
            isFree = true,
            doses = 4,
            intervalDays = listOf(60, 120, 150, 540),
            ageRange = "2、4、5、18月龄",
            notes = "2025年新规：提前至2月龄开始，可被五联疫苗替代"
        ),
        Vaccine(
            id = "dtp_boost",
            name = "DTaP_Boost",
            chineseName = "百白破疫苗（第5剂）",
            description = "预防百日咳、白喉、破伤风（加强免疫）",
            diseaseInfo = DiseaseInfo(
                name = "百日咳",
                description = "百日咳是由百日咳杆菌引起的急性呼吸道传染病，以阵发性痉挛性咳嗽为主要特征。",
                incidenceData = listOf(
                    EpidemicData("2022年百日咳发病率", "0.8/10万", "国家疾控中心"),
                    EpidemicData("青少年和成人发病率上升", "近年来呈上升趋势", "国家疾控中心")
                ),
                outcomes = DiseaseOutcomes(
                    best = "完全康复，无后遗症",
                    worst = "可能发生肺炎、脑病等并发症",
                    typical = "咳嗽持续时间较长"
                )
            ),
            price = 0.0,
            isFree = true,
            doses = 1,
            intervalDays = listOf(2190),
            ageRange = "6周岁",
            notes = "2025年新规：6周岁接种，替代原来的白破疫苗"
        ),
        Vaccine(
            id = "measles",
            name = "MR",
            chineseName = "麻腮风疫苗",
            description = "预防麻疹、流行性腮腺炎、风疹",
            diseaseInfo = DiseaseInfo(
                name = "麻疹",
                description = "麻疹是由麻疹病毒引起的急性呼吸道传染病，传染性极强。",
                incidenceData = listOf(
                    EpidemicData("2022年麻疹发病率", "0.28/10万", "国家疾控中心"),
                    EpidemicData("5岁以下死亡率", "在发展中国家可达3-5%", "WHO")
                ),
                outcomes = DiseaseOutcomes(
                    best = "7-10天完全康复",
                    worst = "可并发肺炎、脑炎导致死亡",
                    typical = "高热、皮疹持续约一周，可能留下疤痕"
                )
            ),
            price = 0.0,
            isFree = true,
            doses = 2,
            intervalDays = listOf(240, 540),
            ageRange = "8月龄、18月龄"
        ),
        Vaccine(
            id = "meningococcal_a",
            name = "MenA",
            chineseName = "A群流脑疫苗",
            description = "预防A群脑膜炎球菌引起的流行性脑脊髓膜炎",
            diseaseInfo = DiseaseInfo(
                name = "流行性脑脊髓膜炎",
                description = "是由脑膜炎奈瑟菌引起的急性化脓性脑膜炎，病情凶险，死亡率高。",
                incidenceData = listOf(
                    EpidemicData("病死率", "可达10%", "中国疾控中心"),
                    EpidemicData("儿童高发年龄段", "6月龄-2岁", "临床数据")
                ),
                outcomes = DiseaseOutcomes(
                    best = "及时治疗可完全康复",
                    worst = "可导致休克、脑疝、死亡",
                    typical = "约10-20%患者留下听力损害、神经系统后遗症"
                )
            ),
            price = 0.0,
            isFree = true,
            doses = 2,
            intervalDays = listOf(180, 270),
            ageRange = "6、9月龄"
        ),
        Vaccine(
            id = "meningococcal_ac",
            name = "MenAC",
            chineseName = "A群C群流脑疫苗",
            description = "预防A群、C群脑膜炎球菌引起的流行性脑脊髓膜炎",
            diseaseInfo = DiseaseInfo(
                name = "流行性脑脊髓膜炎",
                description = "是由脑膜炎奈瑟菌引起的急性化脓性脑膜炎，病情凶险，死亡率高。C群已成为主要流行菌群。",
                incidenceData = listOf(
                    EpidemicData("C群已成为主要流行菌群", "约占60%", "中国疾控中心"),
                    EpidemicData("W135群病死率", "可达20%", "WHO"),
                    EpidemicData("3-6岁高发", "发病率较高年龄段", "临床数据")
                ),
                outcomes = DiseaseOutcomes(
                    best = "及时治疗可完全康复",
                    worst = "可导致休克、脑疝、死亡",
                    typical = "约10-20%患者留下听力损害、神经系统后遗症"
                )
            ),
            price = 0.0,
            isFree = true,
            doses = 2,
            intervalDays = listOf(1095, 2190),
            ageRange = "3周岁、6周岁"
        ),
        Vaccine(
            id = "japanese_encephalitis",
            name = "JE",
            chineseName = "乙脑疫苗",
            description = "预防流行性乙型脑炎",
            diseaseInfo = DiseaseInfo(
                name = "流行性乙型脑炎",
                description = "是由乙型脑炎病毒引起的以脑实质炎症为主要病变的急性传染病，经蚊虫传播。",
                incidenceData = listOf(
                    EpidemicData("病死率", "约5-10%", "中国疾控中心"),
                    EpidemicData("后遗症率", "约30%", "临床统计")
                ),
                outcomes = DiseaseOutcomes(
                    best = "完全康复",
                    worst = "极高热、昏迷、抽搐，可能死亡",
                    typical = "恢复期长，可能留下智力障碍、癫痫等后遗症"
                )
            ),
            price = 0.0,
            isFree = true,
            doses = 2,
            intervalDays = listOf(240, 730),
            ageRange = "8月龄、2周岁"
        ),
        Vaccine(
            id = "hepa",
            name = "HepA",
            chineseName = "甲肝疫苗",
            description = "预防甲型肝炎",
            diseaseInfo = DiseaseInfo(
                name = "甲型肝炎",
                description = "是由甲肝病毒引起的急性肝脏传染病，主要通过粪-口途径传播。",
                incidenceData = listOf(
                    EpidemicData("2022年甲肝发病率", "约1.5/10万", "国家疾控中心"),
                    EpidemicData("儿童感染率", "在卫生条件差地区可达50%", "WHO")
                ),
                outcomes = DiseaseOutcomes(
                    best = "完全康复，获得终身免疫",
                    worst = "极少数发展为急性重型肝炎导致死亡",
                    typical = "2-6周可治愈，愈后良好"
                )
            ),
            price = 0.0,
            isFree = true,
            doses = 1,
            intervalDays = listOf(540),
            ageRange = "18月龄"
        )
    )
    
    val paidVaccines: List<Vaccine> = listOf(
        Vaccine(
            id = "pentaxim",
            name = "Pentaxim",
            chineseName = "五联疫苗",
            description = "预防百日咳、白喉、破伤风、脊髓灰质炎、Hib",
            diseaseInfo = DiseaseInfo(
                name = "五联疫苗可预防的五种疾病",
                description = "五联疫苗可同时预防百日咳、白喉、破伤风、脊髓灰质炎和b型流感嗜血杆菌引起的侵袭性疾病。",
                incidenceData = listOf(
                    EpidemicData("可减少接种次数", "从12剂减至4剂", "疫苗说明书"),
                    EpidemicData("b型流感嗜血杆菌感染", "5岁以下儿童主要致病菌之一", "临床数据")
                ),
                outcomes = DiseaseOutcomes(
                    best = "全程接种后可获得五种疾病的保护",
                    worst = "与单独接种相比，预防效果无差异",
                    typical = "全程接种4剂即可获得保护"
                ),
                comparisonWithFree = "可替代：百白破（4剂）+ 脊髓灰质炎（4剂）+ Hib（4剂），共减少8剂接种"
            ),
            price = 639.0,
            isFree = false,
            doses = 4,
            intervalDays = listOf(60, 90, 120, 540),
            ageRange = "2、3、4、18月龄",
            category = VaccineCategory.REPLACEMENT,
            replaceableVaccines = listOf("dtp", "ipv", "opv", "hib"),
            notes = "可替代百白破、脊髓灰质炎和Hib疫苗"
        ),
        Vaccine(
            id = "hib",
            name = "Hib",
            chineseName = "b型流感嗜血杆菌疫苗",
            description = "预防b型流感嗜血杆菌引起的侵袭性疾病",
            diseaseInfo = DiseaseInfo(
                name = "b型流感嗜血杆菌",
                description = "Hib是5岁以下儿童细菌性脑膜炎的主要致病菌，还可引起肺炎、败血症等严重疾病。",
                incidenceData = listOf(
                    EpidemicData("5岁以下脑膜炎致病菌占比", "约50-60%", "临床研究"),
                    EpidemicData("死亡率", "可达3-6%", "WHO数据"),
                    EpidemicData("后遗症率", "约15-30%", "临床统计")
                ),
                outcomes = DiseaseOutcomes(
                    best = "及时抗生素治疗可完全康复",
                    worst = "可导致死亡或严重神经系统后遗症",
                    typical = "可能留下听力障碍、智力低下、发育迟缓"
                )
            ),
            price = 120.0,
            isFree = false,
            doses = 4,
            intervalDays = listOf(60, 90, 120, 540),
            ageRange = "2、3、4、18月龄",
            category = VaccineCategory.VOLUNTARY
        ),
        Vaccine(
            id = "pcv13",
            name = "PCV13",
            chineseName = "13价肺炎球菌疫苗",
            description = "预防13种血清型肺炎球菌引起的疾病",
            diseaseInfo = DiseaseInfo(
                name = "肺炎球菌性疾病",
                description = "肺炎球菌是引起肺炎、脑膜炎、败血症等严重疾病的主要致病菌，对儿童健康威胁极大。",
                incidenceData = listOf(
                    EpidemicData("中国儿童肺炎发病率", "约3-5%的5岁以下儿童", "中国疾控中心"),
                    EpidemicData("肺炎球菌致病占比", "细菌性肺炎的主要致病菌，约30-50%", "临床研究"),
                    EpidemicData("13价覆盖的血清型", "", "疫苗说明书",
                        listOf(
                            EpidemicData("常见致病血清型", "19A、6B、14、6A等占儿童侵袭性疾病80%以上", "中国疾控中心"),
                            EpidemicData("13价覆盖率", "覆盖中国儿童常见致病血清型的85%以上", "临床研究")
                        )
                    ),
                    EpidemicData("疾病严重程度", "", "临床数据",
                        listOf(
                            EpidemicData("肺炎球菌性脑膜炎病死率", "约5-10%", "WHO"),
                            EpidemicData("肺炎球菌性脑膜炎后遗症率", "约25%", "临床统计"),
                            EpidemicData("肺炎球菌性败血症病死率", "约5-15%", "WHO")
                        )
                    )
                ),
                outcomes = DiseaseOutcomes(
                    best = "及时抗生素治疗可完全康复，无后遗症",
                    worst = "可导致死亡或严重后遗症：听力损失、脑损伤、肢体残疾",
                    typical = "肺炎：咳嗽、发热、呼吸困难，疗程1-2周；脑膜炎：持续高热、头痛、呕吐，约25%留下后遗症"
                ),
                comparisonWithFree = "23价多糖疫苗：适用于2岁以上人群，但免疫持续时间短，对2岁以下儿童效果差。13价结合疫苗：适用于6周龄-5岁儿童，免疫原性强，可诱导免疫记忆，适合婴幼儿接种。"
            ),
            price = 598.0,
            isFree = false,
            doses = 4,
            intervalDays = listOf(60, 120, 180, 420),
            ageRange = "2、4、6、12-15月龄",
            category = VaccineCategory.VOLUNTARY,
            notes = "建议2岁以下婴幼儿优先接种"
        ),
        Vaccine(
            id = "rotavirus",
            name = "RV",
            chineseName = "轮状病毒疫苗",
            description = "预防轮状病毒引起的婴幼儿腹泻",
            diseaseInfo = DiseaseInfo(
                name = "轮状病毒腹泻",
                description = "轮状病毒是引起婴幼儿严重急性腹泻的主要病原体，每年导致约12.5万5岁以下儿童死亡。",
                incidenceData = listOf(
                    EpidemicData("5岁以下儿童腹泻病因占比", "约40%", "WHO"),
                    EpidemicData("全球每年死亡人数", "约12.5万", "WHO 2023"),
                    EpidemicData("中国5岁以下腹泻发病率", "约2-3次/年", "临床数据")
                ),
                outcomes = DiseaseOutcomes(
                    best = "一周左右自愈",
                    worst = "严重脱水、电解质紊乱可导致死亡",
                    typical = "腹泻持续5-7天，需要补液治疗"
                )
            ),
            price = 285.0,
            isFree = false,
            doses = 3,
            intervalDays = listOf(60, 90, 120),
            ageRange = "2、3、4月龄",
            category = VaccineCategory.VOLUNTARY,
            notes = "轮状病毒疫苗为口服疫苗"
        ),
        Vaccine(
            id = "ev71",
            name = "EV71",
            chineseName = "EV71手足口病疫苗",
            description = "预防EV71型肠道病毒引起的手足口病",
            diseaseInfo = DiseaseInfo(
                name = "手足口病（EV71型）",
                description = "EV71型肠道病毒引起的手足口病可导致神经系统并发症，严重者可危及生命。",
                incidenceData = listOf(
                    EpidemicData("手足口病整体发病情况", "每年约200万例，5岁以下儿童为主", "中国疾控中心"),
                    EpidemicData("重症手足口病病原分布", "", "中国疾控中心",
                        listOf(
                            EpidemicData("EV71型占比", "约40%，是最危险的类型", "中国疾控中心"),
                            EpidemicData("Cox A16型占比", "约30%，病情相对较轻", "中国疾控中心"),
                            EpidemicData("其他肠道病毒", "约30%", "中国疾控中心")
                        )
                    ),
                    EpidemicData("EV71型特点", "", "临床资料",
                        listOf(
                            EpidemicData("重症率", "约5-10%", "临床统计"),
                            EpidemicData("死亡率", "约1-3%，最高可达10%", "WHO"),
                            EpidemicData("神经并发症", "约1-5%，包括脑干脑炎、脊髓灰质炎样麻痹", "临床数据")
                        )
                    )
                ),
                outcomes = DiseaseOutcomes(
                    best = "普通型：口腔疱疹、手足皮疹，一周左右自愈",
                    worst = "重型：脑干脑炎、神经源性肺水肿，可迅速死亡，死亡率高达10%",
                    typical = "约0.5-5%发展为重症，表现为持续高热、抽搐、肢体无力，需要住院治疗"
                ),
                comparisonWithFree = "手足口病疫苗目前为二类疫苗（自费）。虽然EV71疫苗只预防EV71型，但该型占重症的40%、死亡的80%，预防价值极高。建议在6月龄-5岁尽早接种。"
            ),
            price = 188.0,
            isFree = false,
            doses = 2,
            intervalDays = listOf(180, 210),
            ageRange = "6月龄-5岁",
            category = VaccineCategory.VOLUNTARY,
            notes = "建议1岁前完成基础免疫"
        ),
        Vaccine(
            id = "influenza",
            name = "Influenza",
            chineseName = "流感疫苗",
            description = "预防季节性流感",
            diseaseInfo = DiseaseInfo(
                name = "季节性流感",
                description = "是由流感病毒引起的急性呼吸道传染病，儿童是高危人群。",
                incidenceData = listOf(
                    EpidemicData("5岁以下儿童流感发病率", "高达20-30%", "WHO"),
                    EpidemicData("5岁以下儿童流感死亡", "全球约1万/年", "WHO 2022"),
                    EpidemicData("中国流感死亡人数", "约8.8万/年（相关）", "中国疾控中心")
                ),
                outcomes = DiseaseOutcomes(
                    best = "一周左右自愈",
                    worst = "可能并发肺炎、心肌炎导致死亡",
                    typical = "高热、全身酸痛持续3-5天"
                )
            ),
            price = 60.0,
            isFree = false,
            doses = 1,
            intervalDays = listOf(180),
            ageRange = "6月龄以上人群",
            category = VaccineCategory.VOLUNTARY,
            notes = "建议每年接种，6月龄-8岁儿童首次接种需2剂"
        ),
        Vaccine(
            id = "varicella",
            name = "Varicella",
            chineseName = "水痘疫苗",
            description = "预防水痘",
            diseaseInfo = DiseaseInfo(
                name = "水痘",
                description = "水痘是由水痘-带状疱疹病毒引起的高度传染性疾病，主要通过飞沫和接触传播。",
                incidenceData = listOf(
                    EpidemicData("发病率", "是儿童常见传染病之一，全年散发", "中国疾控中心"),
                    EpidemicData("传染性", "易感人群接触后发病率高达90%", "临床数据"),
                    EpidemicData("流行特点", "3-6月为高发季节，3-10岁儿童多见", "中国疾控中心"),
                    EpidemicData("并发症情况", "", "临床统计",
                        listOf(
                            EpidemicData("细菌感染", "最常见并发症，约5-10%", "临床数据"),
                            EpidemicData("肺炎", "成人多见，儿童也可发生", "临床统计"),
                            EpidemicData("脑炎", "发生率约0.1-0.2%", "WHO"),
                            EpidemicData("瑞氏综合征", "与阿司匹林相关，死亡率较高", "临床数据")
                        )
                    )
                ),
                outcomes = DiseaseOutcomes(
                    best = "普通型：皮疹、发热，10-14天自愈，愈后良好",
                    worst = "重型：可继发严重细菌感染（脓毒症）、肺炎、脑炎，甚至死亡",
                    typical = "皮疹从头部开始，蔓延至全身，伴有瘙痒，约1-2周结痂脱落后痊愈，可能留下疤痕"
                ),
                comparisonWithFree = "水痘疫苗为二类疫苗（自费）。水痘虽然大部分情况下症状轻微，但传染性极强，且可能留下疤痕。如上幼儿园或小学，建议接种。"
            ),
            price = 150.0,
            isFree = false,
            doses = 2,
            intervalDays = listOf(365, 1460),
            ageRange = "1岁、4岁",
            category = VaccineCategory.VOLUNTARY,
            notes = "建议入托前完成接种"
        ),
        Vaccine(
            id = "meningococcal_acwy",
            name = "MenACWY",
            chineseName = "ACYW135群流脑疫苗",
            description = "预防A、C、Y、W135群脑膜炎球菌引起的流脑",
            diseaseInfo = DiseaseInfo(
                name = "流行性脑脊髓膜炎（多价）",
                description = "流脑是由脑膜炎奈瑟菌引起的急性化脓性脑膜炎，病情凶险，死亡率高。ACYW135可预防A、C、Y、W135四种血清群。",
                incidenceData = listOf(
                    EpidemicData("中国流脑血清群分布", "", "中国疾控中心",
                        listOf(
                            EpidemicData("A群占比", "历史主要流行群，近年来降至约20%", "中国疾控中心"),
                            EpidemicData("C群占比", "约30-40%，为主要流行群之一", "中国疾控中心"),
                            EpidemicData("W135群占比", "约10-15%，病死率最高可达20%", "WHO"),
                            EpidemicData("Y群占比", "约5-10%，近年来有上升趋势", "中国疾控中心")
                        )
                    ),
                    EpidemicData("儿童高发年龄段", "6月龄-2岁", "临床数据"),
                    EpidemicData("各血清群特点", "", "临床资料",
                        listOf(
                            EpidemicData("A群", "发病率最高，但病死率相对较低", "临床统计"),
                            EpidemicData("C群", "目前主要流行株，病情较重", "临床统计"),
                            EpidemicData("W135群", "病死率最高可达20%，需重点预防", "WHO"),
                            EpidemicData("Y群", "近年来感染率上升，扩散迅速", "WHO")
                        )
                    )
                ),
                outcomes = DiseaseOutcomes(
                    best = "及时诊断和治疗可完全康复，不留后遗症",
                    worst = "暴发型流脑可在24小时内死亡，病死率高达20%以上",
                    typical = "普通型流脑：持续高热、头痛、呕吐，7-10天可治愈，约10-20%留下听力损害、智力障碍等后遗症"
                ),
                comparisonWithFree = "免费A群流脑仅能预防A群，无法预防C、Y、W135群。而C群占30-40%，W135群病死率高达20%，建议选择ACYW135获得更全面保护。"
            ),
            price = 85.0,
            isFree = false,
            doses = 2,
            intervalDays = listOf(180, 300),
            ageRange = "6月龄以上",
            category = VaccineCategory.VOLUNTARY,
            notes = "可替代A群流脑疫苗"
        ),
        Vaccine(
            id = "rsv",
            name = "RSV",
            chineseName = "呼吸道合胞病毒单抗",
            description = "预防呼吸道合胞病毒感染",
            diseaseInfo = DiseaseInfo(
                name = "呼吸道合胞病毒（RSV）感染",
                description = "RSV是引起婴幼儿急性下呼吸道感染的主要病原体，尤其在冬季高发，可导致毛细支气管炎和肺炎。",
                incidenceData = listOf(
                    EpidemicData("发病情况", "RSV是婴幼儿肺炎和毛细支气管炎的首要病原体", "中国疾控中心"),
                    EpidemicData("感染年龄", "几乎所有儿童2岁前都会感染RSV", "WHO"),
                    EpidemicData("疾病负担", "", "临床数据",
                        listOf(
                            EpidemicData("5岁以下RSV相关住院", "约3300万人次/年", "WHO 2023"),
                            EpidemicData("6月龄以下死亡", "全球约4.5万/年", "WHO 2022"),
                            EpidemicData("中国住院患儿中RSV占比", "约20-30%", "中国疾控中心")
                        )
                    ),
                    EpidemicData("重症高危人群", "", "临床资料",
                        listOf(
                            EpidemicData("早产儿", "肺发育不成熟，死亡率增加3-10倍", "临床研究"),
                            EpidemicData("先天性心脏病", "心肺负担重，易发展为重症", "临床统计"),
                            EpidemicData("支气管肺发育不良", "基础肺病加重", "临床数据"),
                            EpidemicData("免疫缺陷", "病毒载量高，病情重", "临床资料")
                        )
                    )
                ),
                outcomes = DiseaseOutcomes(
                    best = "普通型：上呼吸道感染症状，一周左右自愈",
                    worst = "重症：需要住院治疗，可出现呼吸衰竭，需使用呼吸机，死亡率约1-3%",
                    typical = "毛细支气管炎：发热、咳嗽、喘息、呼吸困难，病程约1-2周，约1-3%需要住院治疗"
                ),
                comparisonWithFree = "该疫苗为2024年在中国获批上市的非免疫规划疫苗（自费），主要用于高危婴幼儿的预防保护。价格较高，但能有效预防重症RSV感染。"
            ),
            price = 1200.0,
            isFree = false,
            doses = 1,
            intervalDays = listOf(0),
            ageRange = "婴儿出生后首个RSV流行季前",
            category = VaccineCategory.VOLUNTARY,
            notes = "主要适用于早产儿、先天性心脏病、支气管肺发育不良等高危婴儿"
        )
    )
    
    fun generateSchedule(birthDate: LocalDate, selectedPaidVaccines: List<Vaccine> = emptyList()): List<VaccinationRecord> {
        val schedules = mutableListOf<VaccinationRecord>()
        var recordId = 1
        
        val replacedVaccineIds = selectedPaidVaccines
            .filter { it.category == VaccineCategory.REPLACEMENT }
            .flatMap { it.replaceableVaccines }
            .toSet()
        
        val addedPaidVaccineIds = mutableSetOf<String>()
        
        for (vaccine in freeVaccines) {
            if (vaccine.id in replacedVaccineIds) {
                continue
            }
            
            val scheduledDates = calculateVaccinationDates(birthDate, vaccine.doses, vaccine.intervalDays)
            
            for ((index, date) in scheduledDates.withIndex()) {
                schedules.add(
                    VaccinationRecord(
                        id = "free_${recordId++}",
                        vaccine = vaccine,
                        scheduledDate = date,
                        isCompleted = false,
                        isReplaced = false,
                        replacingVaccine = null,
                        doseNumber = index + 1
                    )
                )
            }
        }
        
        for (paidVaccine in selectedPaidVaccines) {
            if (paidVaccine.id in addedPaidVaccineIds) {
                continue
            }
            
            addedPaidVaccineIds.add(paidVaccine.id)
            
            val paidDates = calculateVaccinationDates(birthDate, paidVaccine.doses, paidVaccine.intervalDays)
            for ((index, date) in paidDates.withIndex()) {
                schedules.add(
                    VaccinationRecord(
                        id = "paid_${recordId++}",
                        vaccine = paidVaccine,
                        scheduledDate = date,
                        isCompleted = false,
                        isReplaced = false,
                        replacingVaccine = null,
                        doseNumber = index + 1
                    )
                )
            }
        }
        
        return schedules.sortedBy { it.scheduledDate }
    }
    
    private fun calculateVaccinationDates(
        birthDate: LocalDate,
        doses: Int,
        intervalDays: List<Int>
    ): List<LocalDate> {
        val dates = mutableListOf<LocalDate>()
        
        if (intervalDays.isEmpty()) {
            for (i in 0 until doses) {
                dates.add(birthDate.plusDays((i * 30).toLong()))
            }
        } else {
            for (i in intervalDays) {
                val date = birthDate.plusDays(i.toLong())
                dates.add(date)
            }
        }
        
        return dates
    }
    
    fun getAllVaccines(): List<Vaccine> = freeVaccines + paidVaccines
    
    fun getVaccineById(id: String): Vaccine? = getAllVaccines().find { it.id == id }
}
