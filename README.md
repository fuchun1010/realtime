# realtime


#### insert
```
{
  "header": {
    "version": 1,
    "logfileName": "mysql-bin.000017",
    "logfileOffset": "56509",
    "serverId": "3306",
    "serverenCode": "UTF-8",
    "executeTime": "1546499810000",
    "sourceType": "MYSQL",
    "schemaName": "demo2",
    "tableName": "tab_car",
    "eventLength": "51",
    "eventType": "INSERT",
    "props": [
      {
        "key": "rowsCount",
        "value": "1"
      }
    ]
  },
  "entryType": "ROWDATA",
  "rowChange": {
    "tableId": "234",
    "eventType": "INSERT",
    "isDdl": false,
    "rowDatas": [
      {
        "afterColumns": [
          {
            "index": 0,
            "sqlType": 4,
            "name": "id",
            "isKey": true,
            "updated": true,
            "isNull": false,
            "value": "3",
            "mysqlType": "int"
          },
          {
            "index": 1,
            "sqlType": 12,
            "name": "_name",
            "isKey": false,
            "updated": true,
            "isNull": false,
            "value": "cs",
            "mysqlType": "varchar(50)"
          },
          {
            "index": 2,
            "sqlType": 3,
            "name": "price",
            "isKey": false,
            "updated": true,
            "isNull": false,
            "value": "10000.0",
            "mysqlType": "numeric(10,2)"
          }
        ]
      }
    ]
  }
}

```


#### 统一json
```asciidoc
{
  "op": "insert",
  "db": "demo2",
  "tableName": "tableName",
  "schema": [
    {
      "index": 0,
      "name": "id",
      "pk": true,
      "fieldType": "int"
    },
    {
      "index": 1,
      "name": "_name",
      "pk": false,
      "value": "a",
      "fieldType": "varchar(50)"
    },
    {
      "index": 2,
      "name": "price",
      "pk": false,
      "fieldType": "numeric(10,2)"
    }
  ],
  "data": [
    {
      "index": 0,
      "name": "id",
      "value": "4"
    }, {
      "index": 1,
      "name": "_name",
      "value": "a"
    }, {
      "index": 2,
      "name": "price",
      "value": "620.0"
    }  
    
  ]
}
```
