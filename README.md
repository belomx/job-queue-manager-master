# Job Queue Manager

The Job Queue Manager is an application responsible to receive agents, jobs and requests for jobs.
The application working using file input.

## Usage

cd job-queue-manager

USAGE: lein run -m job-queue-manager.app [ARGS...] 

Argument options:

  -c, --console     Process the file inputed in the console, ctrl+c or ctrl+z to leave 
  
  -f, --file        Process the input json file. Example: lein run -f /home/user/myfile.json 
  
  -d, --directory   Process all the json files from the input directory. Example: lein run -f /home/user/ 
  
  -w, --webapi      Start a server in the specified port. Example: lein run -w 3000
  
  -h, --help        Print the usage and options. 

## Examples on webapi mode (-w):

Endpoint - GET http://<ip>:<port>/jobs

Returns a breakdown of the job queue, consisting of all jobs.

Result:
[
    {
        "id": "f26e890b-df8e-422e-a39c-7762aa0bac36",
        "type": "rewards-question",
        "urgent": false,
        "entry-date": "2018-04-26T17:04:35.699Z",
        "status": "done",
        "working-agent-id": "ed0e23ef-6c2b-430c-9b90-cd4f1ff74c88"
    }
]

Endpoint - GET http://<ip>:<port>/jobs/count_by_agent?agent-id=<agentId>

Given an agent, return how many jobs of each type this agent has performed.

Result:
[
    {
        "rewards-question": 1
    },
    {
        "bills-questions": 0
    }
]

Endpoint - POST http://<ip>:<port>/jobs

Creates a job.

Input:
{
    "id": "f26e890b-df8e-422e-a39c-7762aa0bac36",
    "type": "rewards-question",
    "urgent": false
}

No result in HTTP 200 

Endpoint - POST http://<ip>:<port>/agents

Creates an agent,

Input:
{
  "id": "8ab86c18-3fae-4804-bfd9-c3d6e8f66260",
  "name": "BoJack Horseman",
  "primary_skillset": ["bills-questions"],
  "secondary_skillset": []
}

No result in HTTP 200.

Endpoint - POST http://<ip>:<port>/jobs/request

Assign a job to a agent, and returns the id of the assigned job or indicate the lack of one.

Input:
{
    "agent_id": "8ab86c18-3fae-4804-bfd9-c3d6e8f66260"
}

Result:
{
    "job_id": "c0033410-981c-428a-954a-35dec05ef1d2",
    "agent_id": "8ab86c18-3fae-4804-bfd9-c3d6e8f66260"
}

  
## Swagger on webapi mode

http://<ip>:<port>/job-queue-manager-api-docs
  
## Examples on file type mode (-c, -f, -d):

lein run -m job-queue-manager.app --directory C:\Users\edazfre\Downloads\

-An output folder will be created in the inputed folder.

Open console to insert files in the same (file by file).

lein run -m job-queue-manager.app -c

To process a single file.

lein run -m job-queue-manager.app --file C:\Users\edazfre\Downloads\sample-input.json

Open help screen.

lein run -m job-queue-manager.app --help

Input examples:

To create an agent:

[
  {
    "new_agent": {
      "id": "8ab86c18-3fae-4804-bfd9-c3d6e8f66260",
      "name": "BoJack Horseman",
      "primary_skillset": ["bills-questions"],
      "secondary_skillset": []
    }
  }
]

To create a job:

[
  {
    "new_job": {
      "id": "690de6bc-163c-4345-bf6f-25dd0c58e864",
      "type": "bills-questions",
      "urgent": false
    }
  }
]

To request a job:

[
  {
    "job_request": {
      "agent_id": "8ab86c18-3fae-4804-bfd9-c3d6e8f66260"
    }
  }
]

All together:

[
  {
    "new_agent": {
      "id": "8ab86c18-3fae-4804-bfd9-c3d6e8f66260",
      "name": "BoJack Horseman",
      "primary_skillset": ["bills-questions"],
      "secondary_skillset": []
    }
  },
  {
    "new_job": {
      "id": "690de6bc-163c-4345-bf6f-25dd0c58e864",
      "type": "bills-questions",
      "urgent": false
    }
  },
  {
    "job_request": {
      "agent_id": "8ab86c18-3fae-4804-bfd9-c3d6e8f66260"
    }
  }
]

You are going to receive this in the output:

[
  {
    "job_assigned": {
      "job_id": "690de6bc-163c-4345-bf6f-25dd0c58e864",
      "agent_id": "8ab86c18-3fae-4804-bfd9-c3d6e8f66260"
    }
  }
]

Error messages examples:

In case we forget to put an mandatory value.

[
  [
    {
	  "level":"error","schema": {
	     "loadingURI":"#","pointer":""
	  },
	  "instance": {
	    "pointer":""
	  },
	  "domain":"validation",
	  "keyword":"required",
	  "message":"object has missing required properties ([\"primary_skillset\"])",
	  "required":["id","name","primary_skillset","secondary_skillset"],
	  "missing":["primary_skillset"]
	}
  ]
]

In case you send an invalid request name (the valids are new_agent, new_job, job_request).

[
  {
    "message":
	  {
	    "id":10,
	    "message-text":"The service name is not valid."
	  }
  }
]

## License

Copyright 2018 [Danilo Queiroz Freire](danilo.q.freire@gmail.com)

Distributed under the Eclipse Public License v1.0 (same as Clojure).
