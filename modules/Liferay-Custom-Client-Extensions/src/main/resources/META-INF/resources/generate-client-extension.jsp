<%@ include file="/init.jsp"%>

<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>

<portlet:resourceURL id="/getdata" var="getDataDetailsURL" />
<portlet:actionURL name="generateExtension"
	var="generateExtensionActionURL" />

<div class="container-fluid container-fluid-max-xl">
	<div class="sheet sheet-lg">
		<div class="panel-group panel-group-flush">
			<form id="<portlet:namespace />clientExtensionForm" method="post"
				action="<%= generateExtensionActionURL %>"
				enctype="multipart/form-data">
				<div class="panel panel-unstyled">
					<div class="panel-header">
						<h2 class="panel-title">Client Extension Generator</h2>
					</div>

					<div class="panel-body">
						<!-- Message Container -->
						<div id="<portlet:namespace />messageContainer"
							class="alert alert-dismissible mb-4" style="display: none;">
							<button type="button" class="close" data-dismiss="alert">&times;</button>
							<span id="<portlet:namespace />messageText"></span>
						</div>

						<!-- Form Fields -->
						<div class="form-group">
							<label for="<portlet:namespace />extensionName">Client
								Extension Name *</label> <input type="text" class="form-control"
								id="<portlet:namespace />extensionName"
								name="<portlet:namespace />extensionName" required
								placeholder="my-client-extension"> <small
								class="form-text text-muted">Enter a unique name for
								your client extension</small>
						</div>

						<div class="form-group">
							<label for="<portlet:namespace />extensionType">Extension
								Type *</label> <select class="form-control"
								id="<portlet:namespace />extensionType"
								name="<portlet:namespace />extensionType" required>
								<option value="">-- Select Type --</option>
								<option value="batch">Batch</option>
								<option value="microservice">Microservice</option>
							</select>
						</div>

						<!-- Batch Type Selection (shown only when Batch is selected) -->
						<div class="form-group"
							id="<portlet:namespace />batchTypeContainer"
							style="display: none;">
							<label for="<portlet:namespace />batchType">Batch Types *</label>
							<select class="form-control" id="<portlet:namespace />batchType"
								name="<portlet:namespace />batchType" multiple>
								<option value="picklist">Picklist</option>
								<option value="object">Object</option>
								<option value="objectfolder">Object Folder</option>
							</select> <small class="form-text text-muted">Hold Ctrl/Cmd to
								select multiple types</small>
						</div>

						<!-- Update the data selection container to show items for all selected types -->
						<div class="form-group"
							id="<portlet:namespace />dataSelectionContainer"
							style="display: none;">
							<div id="<portlet:namespace />dynamicSelectionContainers">
								<!-- Dynamic containers for each selected batch type will be added here -->
							</div>
						</div>

						<div class="mt-4">
							<button type="submit" class="btn btn-primary"
								id="<portlet:namespace />generateBtn">Generate ZIP</button>

							<span id="<portlet:namespace />loadingSpinner" class="ml-3"
								style="display: none;"> <span
								class="spinner-border spinner-border-sm" role="status"
								aria-hidden="true"></span> Generating...
							</span>
						</div>
					</div>
				</div>
			</form>
		</div>
	</div>
</div>

<script>
$(document).ready(function() {
    var namespace = '<portlet:namespace />';
    var $form = $('#' + namespace + 'clientExtensionForm');
    var $extensionType = $('#' + namespace + 'extensionType');
    var $batchTypeContainer = $('#' + namespace + 'batchTypeContainer');
    var $batchType = $('#' + namespace + 'batchType');
    var $dataSelectionContainer = $('#' + namespace + 'dataSelectionContainer');
    var $selectedItems = $('#' + namespace + 'selectedItems');
    var $dataSelectionLabel = $('#' + namespace + 'dataSelectionLabel');
    var $selectionHelpText = $('#' + namespace + 'selectionHelpText');
    var $generateBtn = $('#' + namespace + 'generateBtn');
    var $loadingSpinner = $('#' + namespace + 'loadingSpinner');
    var $messageContainer = $('#' + namespace + 'messageContainer');
    var $messageText = $('#' + namespace + 'messageText');

    // Handle extension type change
    $extensionType.on('change', function() {
        var selectedType = $(this).val();
        
        // Reset form elements
        $batchTypeContainer.hide();
        $dataSelectionContainer.hide();
        $selectedItems.empty();
        
        if (selectedType === 'batch') {
            $batchTypeContainer.show();
        }
    });

 // Modify the batch type change handler
    $batchType.on('change', function() {
        var selectedBatchTypes = $(this).val();
        var $dynamicContainers = $('#' + namespace + 'dynamicSelectionContainers');
        
        $dynamicContainers.empty();
        
        if (selectedBatchTypes && selectedBatchTypes.length > 0) {
            $.each(selectedBatchTypes, function(index, batchType) {
                // Create a container for each batch type
                var containerId = namespace + 'selectionContainer_' + batchType;
                var $container = $('<div>').attr('id', containerId).addClass('mb-4');
                
                // Add label and select element
                var labelText = '';
                switch(batchType) {
                    case 'picklist': labelText = 'Select Picklists'; break;
                    case 'object': labelText = 'Select Objects'; break;
                    case 'objectfolder': labelText = 'Select Object Folders'; break;
                }
                
                $container.append(
                    $('<label>').text(labelText),
                    $('<select>')
                        .addClass('form-control')
                        .attr('name', namespace + 'selectedItems_' + batchType)
                        .attr('multiple', true)
                        .append($('<option>').text('Loading...').val(''))
                );
                
                $dynamicContainers.append($container);
                
                // Load items for this batch type
                loadDataItems(batchType, containerId);
            });
            
            $dataSelectionContainer.show();
        } else {
            $dataSelectionContainer.hide();
        }
    });

    // Update loadDataItems function
    function loadDataItems(batchType, containerId) {
        var $select = $('#' + containerId).find('select');
        
        $.ajax({
            url: '<%= getDataDetailsURL %>',
            type: 'POST',
            dataType: 'json',
            data: {
                '<portlet:namespace />batchType': batchType
            },
            success: function(data) {
                $select.empty();
                
                if (data && data.length > 0) {
                    $.each(data, function(index, item) {
                        $select.append(
                            $('<option></option>')
                                .val(item.erc || item.id)
                                .attr('data-id', item.id)
                                .text(item.name)
                        );
                    });
                } else {
                    $select.append(
                        $('<option></option>')
                            .text("No items available")
                            .val("")
                    );
                }
            },
            error: function(xhr, status, error) {
                console.error("Error loading data items: ", error);
                $select.empty().append(
                    $('<option></option>')
                        .text("Error loading items")
                        .val("")
                );
            }
        });
    }
    // Form submission handler (keep existing logic)
    $form.on('submit', function(e) {
        e.preventDefault();
        $generateBtn.prop('disabled', true);
        $loadingSpinner.show();
        
        var formData = new FormData(this);
        
        $.ajax({
            url: $(this).attr('action'),
            type: 'POST',
            data: formData,
            processData: false,
            contentType: false,
            xhrFields: {
                responseType: 'blob'
            },
            success: function(data, status, xhr) {
                // Handle file download (existing logic)
                var blob = new Blob([data]);
                var fileName = "client-extension.zip";
                var contentDisposition = xhr.getResponseHeader('content-disposition');
                
                if (contentDisposition) {
                    var fileNameMatch = contentDisposition.match(/filename="(.+)"/);
                    if (fileNameMatch && fileNameMatch.length === 2) {
                        fileName = fileNameMatch[1];
                    }
                }
                
                var link = document.createElement('a');
                link.href = window.URL.createObjectURL(blob);
                link.download = fileName;
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
                
                showMessage('success', 'Client extension generated successfully!');
            },
            error: function(xhr) {
                var errorMsg = 'Error generating client extension';
                try {
                    var jsonResponse = JSON.parse(xhr.responseText);
                    if (jsonResponse && jsonResponse.message) {
                        errorMsg = jsonResponse.message;
                    }
                } catch (e) {
                    console.error('Error parsing error response', e);
                }
                showMessage('danger', errorMsg);
            },
            complete: function() {
                $generateBtn.prop('disabled', false);
                $loadingSpinner.hide();
            }
        });
    });
    
    function showMessage(type, message) {
        $messageContainer.removeClass('alert-success alert-danger')
                       .addClass('alert-' + type)
                       .show();
        $messageText.text(message);
        
        setTimeout(function() {
            $messageContainer.fadeOut();
        }, 5000);
    }
    
    $messageContainer.on('click', '.close', function() {
        $messageContainer.hide();
    });
});
</script>