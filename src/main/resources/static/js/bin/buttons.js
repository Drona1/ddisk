$(document).ready(function () {
    assignButtons();
});

function assignButtons() {
    $('#deleteButton').click(function () {
        const addressList= getAddressList();
        $.post("/delete", addressList, function () {
            window.location.reload();
        });
    });
    $('#restoreButton').click(function () {
        let addressList = getAddressList();
        $.post("/restore", addressList, function () {
            window.location.reload();
        });
    });
}
function getAddressList(){
    const addressList= {'selectedObjects[]': []};
    $(":checked").each(function () {
        if ($(this).val() != "on") {
            addressList['selectedObjects[]'].push($(this).val());
        }
    });
    return addressList;
}