## 接口文档

### 获取相册概要
GET: http://dili.bdatu.com/jiekou/mains/p${index}.html

接口数据:
``` md
{
	"total": "1102",
	"page": "1",
	"pagecount": "15",
	"album": [{
		"id": "1611",
		"title": "2017-06-25 每日精选",
		"url": "http://pic01.bdatu.com/Upload/appsimg/1497947485.jpg",
		"addtime": "2017-06-25 00:04:00",
		"adshow": "NO",
		"fabu": "YES",
		"encoded": "1",
		"amd5": "ec653779bb1fef953a0b998e19e21bf3",
		"sort": "1619",
		"ds": "1",
		"timing": "1",
		"timingpublish": "2017-06-25 00:00:00"
	}]
}
```

### 获取照片详情
GET: http://dili.bdatu.com/jiekou/albums/a755.html

接口数据:
```md
{
    "counttotal": "12",
    "picture": [
        {
            "id": "14457",
            "albumid": "1601",
            "title": "〈寻找真正的维京人〉精选",
            "content": "波兰的维京人重演者穿上盔甲，准备上演近身战斗。维京人的残暴并非浪得虚名：斯堪地那维亚男孩从小就接受作战训练，并且在社会制约下习于血腥暴力。 ",
            "url": "http://pic01.bdatu.com/Upload/picimg/1496230162.jpg",
            "size": "254945",
            "addtime": "2017-05-31 19:29:25",
            "author": "David Guttenfelder",
            "thumb": "http://pic01.bdatu.com/Upload/picimg/1496230162.jpg",
            "encoded": "1",
            "weburl": "http://",
            "type": "pic",
            "yourshotlink": "",
            "copyright": "",
            "pmd5": "9f4c2c9dd41064b5bf5bd467bac8956e",
            "sort": "14457"
        }
    ]
}
```