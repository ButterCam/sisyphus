plugins {
    id("com.netflix.nebula.contacts")
}

contacts {
    addPerson("higan@live.cn", delegateClosureOf<nebula.plugin.contacts.Contact> {
        moniker = "higan"
        github = "devkanro"
        roles.add("owner")
    })
}
