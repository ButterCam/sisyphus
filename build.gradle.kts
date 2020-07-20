plugins {
    id("nebula.contacts")
}

contacts {
    addPerson("higan@live.cn", delegateClosureOf<nebula.plugin.contacts.Contact> {
        moniker = "higan"
        github = "devkanro"
        roles.add("owner")
    })
    addPerson("wzlylv@163.com", delegateClosureOf<nebula.plugin.contacts.Contact> {
        moniker = "wangzheng"
        github = "GuoDuanLZ"
        roles.add("maintainer")
    })
    addPerson("zhaoy_xin@163.com", delegateClosureOf<nebula.plugin.contacts.Contact> {
        moniker = "future"
        github = "yuxin-zhao"
        roles.add("maintainer")
    })
    addPerson("jane.zhangjin@outlook.com", delegateClosureOf<nebula.plugin.contacts.Contact> {
        moniker = "ZhangJin"
        github = "ZhangJin233"
        roles.add("tester")
    })
}
