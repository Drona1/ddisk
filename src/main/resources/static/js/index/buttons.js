$(document).ready(function () {
    assignButtons();
});

function assignButtons() {
    $('#rename').click(function () {
        const checkboxes = document.querySelectorAll('input[type=checkbox]');
        for (let i = 0; i < checkboxes.length; i++) {
            if (checkboxes[i].checked) {
                const currentAddress = $("input:checked").get()[0].value;
                const currentName = checkboxes[i].closest('tr').querySelectorAll('td')[1].textContent.trim();
                const renameAddress = document.querySelector('#renameAddress');
                renameAddress.value = currentAddress;
                const newName = document.querySelector('#newName');
                newName.value = currentName;
                break;
            }
        }
    });
    $('#downloadButton').click(function () {
        const addressList = getAddressList();

        $.ajax({
            url: "/download",
            type: "POST",
            data: addressList,
            xhrFields: {
                responseType: 'blob'
            },
            success: function (data, status, xhr) {
                if (xhr.status == 200) {
                    const downloadLink = document.createElement('a');
                    downloadLink.href = window.URL.createObjectURL(data);
                    downloadLink.download = 'files.zip';

                    document.body.appendChild(downloadLink);
                    downloadLink.click();
                    document.body.removeChild(downloadLink);
                }
            },
            error: function (xhr, status, error) {
            }
        });
    });

    $('#removeButton').click(function () {
        const addressList = getAddressList();
        $.post("/remove", addressList, function () {
            window.location.reload();
        });
    });

    $('#copyLink').click(function () {
        navigator.clipboard.writeText($(this).val());
    });

    $('#saveShareButton').click(function () {
        const table = document.getElementById('tableShare');
        let users = [];
        let accessRights = [];
        let gar = document.getElementById('globalAccessRight').innerHTML.trim();
        let globalAccessRight = null;
        let currentObj = $("input:checked").get()[0].value;

        if (gar != "Restricted") {
            globalAccessRight = document.getElementById('globalButton').innerHTML.trim();
        }
        table.querySelectorAll('tr').forEach(element => {
            if (element.rowIndex > 1) {
                users.push(element.cells[0].textContent.trim());
                accessRights.push(element.cells[1].querySelector('button').innerHTML.trim());
            }
        });
        $.post("/share",
            {
                users: users,
                accessRights: accessRights,
                globalAccessRight: globalAccessRight,
                currentObj: currentObj
            }, function (data, status) {

                if (status === "success") {
                    window.location.reload();
                } else {
                    console.log("no changes");
                    document.getElementById("cancelShareButton").click();
                }
            });
    });
}

function getAddressList() {
    const addressList = {'selectedObjects[]': []};
    $(":checked").each(function () {
        if ($(this).val() !== "on") {
            addressList['selectedObjects[]'].push($(this).val());
        }
    });
    return addressList;
}